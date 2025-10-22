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
import com.bookrecommend.book_recommend_be.service.file.PdfToEpubConverter;
import com.bookrecommend.book_recommend_be.service.file.StoredFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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
    private final PdfToEpubConverter pdfToEpubConverter;

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
    public BookResponse createBook(BookRequest request) {

        validateBookInput(request.getTitle(), request.getDescription(), request.getAuthorNames(), request.getGenreIds());

        MultipartFile coverFile = request.getCover();
        MultipartFile file = request.getFile();

        if (coverFile == null || coverFile.isEmpty()) {
            throw new IllegalArgumentException("Cover image is required");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must be provided");
        }

        ImageUploadResponse uploadResponse = cloudinaryService.uploadImage(coverFile, "book_recommend/books");
        String coverImageUrl = uploadResponse.getSecureUrl() != null
                ? uploadResponse.getSecureUrl()
                : uploadResponse.getUrl();


        if (!fileStorageService.isValidBookFile(file)) {
            throw new IllegalArgumentException("Invalid file type. Only supported book files are allowed");
        }

        String title = request.getTitle();
        Book book = new Book();
        book.setTitle(title);
        book.setDescription(request.getDescription());
        book.setPublicationYear(request.getPublicationYear());
        book.setPublisher(request.getPublisher());
        book.setCoverImageUrl(coverImageUrl);
        book.setAuthors(resolveAuthors(request.getAuthorNames()));
        book.setGenres(new HashSet<>(validateGenres(request.getGenreIds())));

        List<String> uploadedObjectKeys = new ArrayList<>();

        try {
            String formatType = detectFormatType(file);
            boolean isPdf = fileStorageService.isPdfFile(file);

            StoredFile storedOriginal = fileStorageService.storeFile(file, title, formatType);
            uploadedObjectKeys.add(storedOriginal.objectKey());

            List<BookFormat> formats = new ArrayList<>();
            BookFormat originalFormat = createBookFormat(book, formatType, storedOriginal);
            formats.add(originalFormat);

            if (isPdf) {
                byte[] pdfBytes = file.getBytes();
                PdfToEpubConverter.ConversionResult conversionResult = pdfToEpubConverter.convert(pdfBytes, title);
                StoredFile storedEpub = fileStorageService.storeFile(
                        conversionResult.epubBytes(),
                        conversionResult.fileName(),
                        "application/epub+zip",
                        title,
                        "EPUB",
                        conversionResult.totalPages());
                uploadedObjectKeys.add(storedEpub.objectKey());

                // Update total pages for PDF format using original document metadata
                originalFormat.setTotalPages(conversionResult.totalPages());

                BookFormat epubFormat = createBookFormat(book, "EPUB", storedEpub);
                epubFormat.setTotalPages(conversionResult.totalPages());
                formats.add(epubFormat);
            }

            book.setFormats(formats);

            Book savedBook = bookRepository.save(book);

            return mapToBookResponse(savedBook);

        } catch (Exception e) {
            uploadedObjectKeys.forEach(fileStorageService::deleteFile);
            throw new RuntimeException("Failed to create book with file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = findBookOrThrow(id);

        // --- Update thông tin cơ bản ---
        book.setTitle(request.getTitle());
        book.setDescription(request.getDescription());
        book.setPublicationYear(request.getPublicationYear());
        book.setPublisher(request.getPublisher());
        book.setAuthors(resolveAuthors(request.getAuthorNames()));
        book.setGenres(new HashSet<>(validateGenres(request.getGenreIds())));

        // --- Update ảnh bìa nếu có ---
        MultipartFile coverFile = request.getCover();
        if (coverFile != null && !coverFile.isEmpty()) {
            ImageUploadResponse upload = cloudinaryService.uploadImage(coverFile, "book_recommend/books");
            String newUrl = StringUtils.hasText(upload.getSecureUrl()) ? upload.getSecureUrl() : upload.getUrl();
            book.setCoverImageUrl(newUrl);
        }

        // --- Update file nếu có ---
        MultipartFile file = request.getFile();
        if (file != null && !file.isEmpty()) {
            if (!fileStorageService.isValidBookFile(file))
                throw new IllegalArgumentException("Invalid file type");

            List<String> rollbackKeys = new ArrayList<>();
            try {
                String formatType = detectFormatType(file);
                boolean isPdf = fileStorageService.isPdfFile(file);

                // Upload bản chính
                StoredFile stored = fileStorageService.storeFile(file, book.getTitle(), formatType);
                rollbackKeys.add(stored.objectKey());

                BookFormat target = book.getFormats().stream()
                        .filter(f -> f.getType().getName().equalsIgnoreCase(formatType))
                        .findFirst()
                        .orElseGet(() -> {
                            BookFormat f = new BookFormat();
                            f.setBook(book);
                            f.setType(getOrCreateBookType(formatType));
                            book.getFormats().add(f);
                            return f;
                        });

                target.setContentUrl(stored.objectKey());
                target.setFileSizeKb(fileStorageService.calculateFileSizeKb(stored.sizeBytes()));

                // Convert PDF -> EPUB
                if (isPdf) {
                    PdfToEpubConverter.ConversionResult conv = pdfToEpubConverter.convert(file.getBytes(), book.getTitle());
                    StoredFile epub = fileStorageService.storeFile(
                            conv.epubBytes(), conv.fileName(), "application/epub+zip",
                            book.getTitle(), "EPUB", conv.totalPages());
                    rollbackKeys.add(epub.objectKey());

                    BookFormat epubFormat = book.getFormats().stream()
                            .filter(f -> f.getType().getName().equalsIgnoreCase("EPUB"))
                            .findFirst()
                            .orElseGet(() -> {
                                BookFormat newFmt = new BookFormat();
                                newFmt.setBook(book);
                                newFmt.setType(getOrCreateBookType("EPUB"));
                                book.getFormats().add(newFmt);
                                return newFmt;
                            });
                    epubFormat.setContentUrl(epub.objectKey());
                    epubFormat.setFileSizeKb(fileStorageService.calculateFileSizeKb(epub.sizeBytes()));
                    epubFormat.setTotalPages(conv.totalPages());
                }

            } catch (Exception e) {
                rollbackKeys.forEach(fileStorageService::deleteFile);
                throw new RuntimeException("Update failed: " + e.getMessage(), e);
            }
        }

        Book saved = bookRepository.save(book);
        return mapToBookResponse(saved);
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

        String objectKey = book.getFormats().stream()
                .filter(f -> f.getId().equals(formatId))
                .map(BookFormat::getContentUrl)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Format not found with id: " + formatId));

        return fileStorageService.generatePresignedUrl(objectKey);
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
                .map(format -> {
                    String contentUrl = null;
                    if (StringUtils.hasText(format.getContentUrl())) {
                        try {
                            contentUrl = fileStorageService.generatePresignedUrl(format.getContentUrl());
                        } catch (Exception ex) {
                            log.warn("Failed to generate presigned URL for object {}: {}", format.getContentUrl(), ex.getMessage());
                            contentUrl = format.getContentUrl();
                        }
                    }
                    return new BookResponse.FormatInfo(
                            format.getId(),
                            format.getType().getName(),
                            format.getTotalPages(),
                            format.getFileSizeKb(),
                            contentUrl);
                })
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

    private BookFormat createBookFormat(Book book, String formatType, StoredFile storedFile) {
        BookFormat format = new BookFormat();
        format.setBook(book);
        format.setType(getOrCreateBookType(formatType));
        format.setContentUrl(storedFile.objectKey());
        format.setFileSizeKb(fileStorageService.calculateFileSizeKb(storedFile.sizeBytes()));
        format.setTotalPages(storedFile.totalPages());
        return format;
    }
}
