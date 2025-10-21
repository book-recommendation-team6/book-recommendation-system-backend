package com.bookrecommend.book_recommend_be.service.book;

import com.bookrecommend.book_recommend_be.dto.request.BookRequest;
import com.bookrecommend.book_recommend_be.dto.response.BookResponse;
import com.bookrecommend.book_recommend_be.dto.response.ImageUploadResponse;
import com.bookrecommend.book_recommend_be.exceptions.ResourceNotFoundException;
import com.bookrecommend.book_recommend_be.model.*;
import com.bookrecommend.book_recommend_be.repository.AuthorRepository;
import com.bookrecommend.book_recommend_be.repository.BookRepository;
import com.bookrecommend.book_recommend_be.repository.BookTypeRepository;
import com.bookrecommend.book_recommend_be.repository.GenreRepository;
import com.bookrecommend.book_recommend_be.service.file.CloudinaryService;
import com.bookrecommend.book_recommend_be.service.file.IFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService implements IBookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final BookTypeRepository bookTypeRepository;
    private final IFileStorageService fileStorageService;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper modelMapper;

    @Override
    public Page<BookResponse> getBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findAll(pageable)
                .map(this::mapToBookResponse);
    }

    @Override
    public Page<BookResponse> getNewestBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findNewestBooks(pageable)
                .map(this::mapToBookResponse);
    }

    @Override
    public Page<BookResponse> getMostReadBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findMostReadBooks(pageable)
                .map(this::mapToBookResponse);
    }

    @Override
    public Page<BookResponse> getBooksByGenre(Long genreId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findBooksByGenre(genreId, pageable)
                .map(this::mapToBookResponse);
    }

    @Override
    public BookResponse getBookById(Long id) {
        Book book = findBookOrThrow(id);
        return mapToBookResponse(book);
    }

    @Override
    @Transactional
    public BookResponse createBook(BookRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setDescription(request.getDescription());
        book.setCoverImageUrl(request.getCoverImageUrl());
        book.setPublicationYear(request.getPublicationYear());
        book.setPublisher(request.getPublisher());
        book.setAuthors(resolveAuthors(request.getAuthorNames()));
        book.setGenres(new HashSet<>(validateGenres(request.getGenreIds())));

        if (request.getFormats() == null || request.getFormats().isEmpty()) {
            throw new IllegalArgumentException("Book must have at least one format");
        }

        List<BookFormat> formats = request.getFormats().stream()
                .map(f -> {
                    BookType type = bookTypeRepository.findById(f.getTypeId())
                            .orElseThrow(() -> new ResourceNotFoundException("Format type not found"));
                    BookFormat format = new BookFormat();
                    format.setBook(book);
                    format.setType(type);
                    format.setContentUrl(f.getContentUrl());
                    format.setTotalPages(f.getTotalPages());
                    format.setFileSizeKb(f.getFileSizeKb());
                    return format;
                })
                .toList();

        book.setFormats(formats);

        Book savedBook = bookRepository.save(book);
        return modelMapper.map(savedBook, BookResponse.class);
    }

    @Override
    public ImageUploadResponse uploadCoverImage(MultipartFile coverFile) {
        if (coverFile == null || coverFile.isEmpty()) {
            throw new IllegalArgumentException("Cover image file must not be empty");
        }
        return cloudinaryService.uploadImage(coverFile, "book_recommend/books");
    }

    @Override
    @Transactional
    public BookResponse updateCoverImage(Long bookId, MultipartFile coverFile) {
        if (coverFile == null || coverFile.isEmpty()) {
            throw new IllegalArgumentException("Cover image file must not be empty");
        }

        Book book = findBookOrThrow(bookId);
        String previousCoverUrl = book.getCoverImageUrl();

        ImageUploadResponse uploadResponse = cloudinaryService.uploadImage(coverFile, "book_recommend/books");
        String resolvedUrl = StringUtils.hasText(uploadResponse.getSecureUrl())
                ? uploadResponse.getSecureUrl()
                : uploadResponse.getUrl();

        book.setCoverImageUrl(resolvedUrl);
        Book savedBook = bookRepository.save(book);

        if (StringUtils.hasText(previousCoverUrl) && !previousCoverUrl.equals(resolvedUrl)) {
            cloudinaryService.extractPublicIdFromUrl(previousCoverUrl)
                    .ifPresent(publicId -> {
                        try {
                            cloudinaryService.deleteImage(publicId);
                        } catch (Exception ex) {
                            log.warn("Failed to delete old book cover from Cloudinary: {}", publicId, ex);
                        }
                    });
        }

        return mapToBookResponse(savedBook);
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = findBookOrThrow(id);

        modelMapper.typeMap(BookRequest.class, Book.class)
                .addMappings(mapper -> {
                    mapper.skip(Book::setAuthors);
                    mapper.skip(Book::setGenres);
                    mapper.skip(Book::setFormats);
                })
                .map(request, book);

        book.setAuthors(resolveAuthors(request.getAuthorNames()));
        book.setGenres(new HashSet<>(validateGenres(request.getGenreIds())));

        book.getFormats().clear();
        List<BookFormat> formats = request.getFormats().stream()
                .map(f -> {
                    BookType type = bookTypeRepository.findById(f.getTypeId())
                            .orElseThrow(() -> new ResourceNotFoundException("Format type not found"));
                    BookFormat format = new BookFormat();
                    format.setBook(book);
                    format.setType(type);
                    format.setContentUrl(f.getContentUrl());
                    format.setTotalPages(f.getTotalPages());
                    format.setFileSizeKb(f.getFileSizeKb());
                    return format;
                })
                .toList();
        book.getFormats().addAll(formats);

        Book updatedBook = bookRepository.save(book);
        return mapToBookResponse(updatedBook);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = findBookOrThrow(id);

        // Delete physical files before deleting from database
        List<String> fileUrls = book.getFormats().stream()
                .map(BookFormat::getContentUrl)
                .toList();

        log.info("Deleting book {} with {} files", id, fileUrls.size());

        // Delete from database first (cascade will delete formats)
        bookRepository.delete(book);

        // Then delete physical files
        fileUrls.forEach(url -> {
            try {
                fileStorageService.deleteFile(url);
            } catch (Exception e) {
                log.warn("Failed to delete file: {}, error: {}", url, e.getMessage());
            }
        });

        log.info("Book {} deleted successfully", id);
    }

    @Override
    @Transactional
    public BookResponse createBookWithFiles(
            String title,
            String description,
            String coverImageUrl,
            Integer publicationYear,
            String publisher,
            List<String> authorNames,
            List<Long> genreIds,
            MultipartFile file) {

        log.info("Creating book with file: {}", title);

        validateBookInput(title, description, authorNames, genreIds);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must be provided");
        }

        List<Genre> genres = validateGenres(genreIds);

        Book book = new Book();
        book.setTitle(title);
        book.setDescription(description);
        book.setCoverImageUrl(coverImageUrl);
        book.setPublicationYear(publicationYear);
        book.setPublisher(publisher);
        book.setAuthors(resolveAuthors(authorNames));
        book.setGenres(new HashSet<>(genres));

        String uploadedFileUrl = null;

        try {
            // Detect format type from file extension
            String formatType = detectFormatType(file);

            // Upload file
            uploadedFileUrl = fileStorageService.storeFile(file, title, formatType);

            // Create book format
            BookType bookType = getOrCreateBookType(formatType);
            BookFormat format = new BookFormat();
            format.setBook(book);
            format.setType(bookType);
            format.setContentUrl(uploadedFileUrl);
            format.setTotalPages(null);
            format.setFileSizeKb(fileStorageService.getFileSizeInKb(file));

            book.setFormats(List.of(format));
            Book savedBook = bookRepository.save(book);

            log.info("Book created successfully with {} format", formatType);
            return mapToBookResponse(savedBook);

        } catch (Exception e) {
            log.error("Failed to create book, rolling back uploaded file", e);
            if (uploadedFileUrl != null) {
                fileStorageService.deleteFile(uploadedFileUrl);
            }
            throw new RuntimeException("Failed to create book with file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public BookResponse updateBookWithFiles(
            Long id,
            String title,
            String description,
            String coverImageUrl,
            Integer publicationYear,
            String publisher,
            List<String> authorNames,
            List<Long> genreIds,
            MultipartFile file) {

        log.info("Updating book with file: {}", id);

        Book book = findBookOrThrow(id);

        book.setTitle(title);
        book.setDescription(description);
        book.setCoverImageUrl(coverImageUrl);
        book.setPublicationYear(publicationYear);
        book.setPublisher(publisher);
        book.setAuthors(resolveAuthors(authorNames));
        book.setGenres(new HashSet<>(validateGenres(genreIds)));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must be provided");
        }

        String oldFileUrl = null;
        String newFileUrl = null;

        try {
            // Detect format type from file
            String formatType = detectFormatType(file);

            // Find existing format of this type
            BookFormat existingFormat = book.getFormats().stream()
                    .filter(f -> f.getType().getName().equalsIgnoreCase(formatType))
                    .findFirst()
                    .orElse(null);

            // Upload new file
            newFileUrl = fileStorageService.storeFile(file, title, formatType);

            if (existingFormat != null) {
                // Update existing format
                oldFileUrl = existingFormat.getContentUrl();
                existingFormat.setContentUrl(newFileUrl);
                existingFormat.setTotalPages(null);
                existingFormat.setFileSizeKb(fileStorageService.getFileSizeInKb(file));
                log.info("Updated existing {} format", formatType);
            } else {
                // Add new format
                BookType bookType = getOrCreateBookType(formatType);
                BookFormat newFormat = new BookFormat();
                newFormat.setBook(book);
                newFormat.setType(bookType);
                newFormat.setContentUrl(newFileUrl);
                newFormat.setTotalPages(null);
                newFormat.setFileSizeKb(fileStorageService.getFileSizeInKb(file));
                book.getFormats().add(newFormat);
                log.info("Added new {} format", formatType);
            }

            Book updatedBook = bookRepository.save(book);

            // Delete old file after successful save
            if (oldFileUrl != null) {
                fileStorageService.deleteFile(oldFileUrl);
            }

            log.info("Book updated successfully with {} format", formatType);
            return mapToBookResponse(updatedBook);

        } catch (Exception e) {
            log.error("Failed to update book, rolling back uploaded file", e);
            if (newFileUrl != null) {
                fileStorageService.deleteFile(newFileUrl);
            }
            throw new RuntimeException("Failed to update book with file: " + e.getMessage(), e);
        }
    }

    @Override
    public Page<BookResponse> searchBooks(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.searchBooks(keyword, pageable)
                .map(this::mapToBookResponse);
    }

    @Override
    public String getBookFormatUrl(Long bookId, Long formatId) {
        Book book = findBookOrThrow(bookId);

        return book.getFormats().stream()
                .filter(f -> f.getId().equals(formatId))
                .map(BookFormat::getContentUrl)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Format not found with id: " + formatId));
    }

    private BookResponse mapToBookResponse(Book book) {
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setDescription(book.getDescription());
        response.setCoverImageUrl(book.getCoverImageUrl());
        response.setPublicationYear(book.getPublicationYear());
        response.setPublisher(book.getPublisher());
        response.setCreatedAt(book.getCreatedAt());
        response.setUpdatedAt(book.getUpdatedAt());

        // Map authors
        Set<BookResponse.AuthorInfo> authors = book.getAuthors().stream()
                .map(author -> new BookResponse.AuthorInfo(author.getId(), author.getName()))
                .collect(java.util.stream.Collectors.toSet());
        response.setAuthors(authors);

        // Map genres
        Set<BookResponse.GenreInfo> genres = book.getGenres().stream()
                .map(genre -> new BookResponse.GenreInfo(genre.getId(), genre.getName()))
                .collect(java.util.stream.Collectors.toSet());
        response.setGenres(genres);

        // Map formats
        List<BookResponse.FormatInfo> formats = book.getFormats().stream()
                .map(format -> new BookResponse.FormatInfo(
                        format.getId(),
                        format.getType().getName(),
                        format.getTotalPages(),
                        format.getFileSizeKb(),
                        format.getContentUrl()))
                .collect(java.util.stream.Collectors.toList());
        response.setFormats(formats);

        return response;
    }

    private Book findBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    private void validateBookInput(String title, String description, List<String> authorNames, List<Long> genreIds) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (authorNames == null || authorNames.isEmpty()) {
            throw new IllegalArgumentException("At least one author is required");
        }
        if (genreIds == null || genreIds.isEmpty()) {
            throw new IllegalArgumentException("At least one genre is required");
        }
    }

    private List<Genre> validateGenres(List<Long> genreIds) {
        List<Genre> genres = genreRepository.findAllById(genreIds);
        if (genres.size() != genreIds.size()) {
            throw new ResourceNotFoundException("One or more genres not found");
        }
        return genres;
    }

    private Set<Author> resolveAuthors(List<String> authorNames) {
        Set<Author> authors = new HashSet<>();
        for (String authorName : authorNames) {
            Author author = authorRepository.findByNameIgnoreCase(authorName)
                    .orElseGet(() -> {
                        Author newAuthor = new Author();
                        newAuthor.setName(authorName);
                        return authorRepository.save(newAuthor);
                    });
            authors.add(author);
        }
        return authors;
    }

    private String detectFormatType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("File name is empty");
        }

        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex + 1).toLowerCase();
        }

        return switch (extension) {
            case "pdf" -> "PDF";
            case "docx" -> "DOCX";
            case "doc" -> "DOC";
            default -> throw new IllegalArgumentException("Unsupported file type. Only PDF, DOCX, and DOC are allowed");
        };
    }

    private synchronized BookType getOrCreateBookType(String typeName) {
        return bookTypeRepository.findByName(typeName)
                .orElseGet(() -> {
                    try {
                        BookType newType = new BookType();
                        newType.setName(typeName);
                        return bookTypeRepository.save(newType);
                    } catch (Exception e) {
                        // Handle unique constraint violation (race condition)
                        log.warn("Race condition creating BookType '{}', retrying fetch", typeName);
                        return bookTypeRepository.findByName(typeName)
                                .orElseThrow(() -> new RuntimeException("Failed to create or fetch BookType"));
                    }
                });
    }
}
