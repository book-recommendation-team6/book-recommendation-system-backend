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
import com.bookrecommend.book_recommend_be.service.file.DownloadedFile;
import com.bookrecommend.book_recommend_be.service.file.IFileStorageService;
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

import java.text.Normalizer;
import java.util.*;

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
    @Override
    public Page<BookResponse> getBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookRepository.findByIsDeletedFalse(pageable)
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

        book.setDeleted(true);
        bookRepository.save(book);

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
        if (coverFile == null || coverFile.isEmpty()) {
            throw new IllegalArgumentException("Cover image is required");
        }

        MultipartFile pdfFile = request.getPdfFile();
        MultipartFile epubFile = request.getEpubFile();

        validateRequiredFormatFile(pdfFile, "PDF", "pdf");
        validateRequiredFormatFile(epubFile, "EPUB", "epub");

        ImageUploadResponse uploadResponse = cloudinaryService.uploadImage(coverFile, "book_recommend/books");
        String coverImageUrl = uploadResponse.getSecureUrl() != null
                ? uploadResponse.getSecureUrl()
                : uploadResponse.getUrl();

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
            List<BookFormat> formats = new ArrayList<>();

            StoredFile pdfStored = fileStorageService.storeFile(pdfFile, title, "PDF");
            uploadedObjectKeys.add(pdfStored.objectKey());
            formats.add(createBookFormat(book, "PDF", pdfStored));

            StoredFile epubStored = fileStorageService.storeFile(epubFile, title, "EPUB");
            uploadedObjectKeys.add(epubStored.objectKey());
            formats.add(createBookFormat(book, "EPUB", epubStored));

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

        book.setTitle(request.getTitle());
        book.setDescription(request.getDescription());
        book.setPublicationYear(request.getPublicationYear());
        book.setPublisher(request.getPublisher());
        book.setAuthors(resolveAuthors(request.getAuthorNames()));
        book.setGenres(new HashSet<>(validateGenres(request.getGenreIds())));

        MultipartFile coverFile = request.getCover();
        if (coverFile != null && !coverFile.isEmpty()) {
            ImageUploadResponse upload = cloudinaryService.uploadImage(coverFile, "book_recommend/books");
            String newUrl = StringUtils.hasText(upload.getSecureUrl()) ? upload.getSecureUrl() : upload.getUrl();
            book.setCoverImageUrl(newUrl);
        }

        MultipartFile newPdfFile = request.getPdfFile();
        MultipartFile newEpubFile = request.getEpubFile();

        boolean hasPdfUpdate = newPdfFile != null && !newPdfFile.isEmpty();
        boolean hasEpubUpdate = newEpubFile != null && !newEpubFile.isEmpty();

        if (hasPdfUpdate) {
            validateFormatFile(newPdfFile, "PDF", "pdf");
        }
        if (hasEpubUpdate) {
            validateFormatFile(newEpubFile, "EPUB", "epub");
        }

        if (hasPdfUpdate || hasEpubUpdate) {
            List<String> rollbackKeys = new ArrayList<>();
            try {
                if (hasPdfUpdate) {
                    StoredFile storedPdf = fileStorageService.storeFile(newPdfFile, book.getTitle(), "PDF");
                    rollbackKeys.add(storedPdf.objectKey());
                    BookFormat pdfFormat = getOrCreateFormat(book, "PDF");
                    applyStoredFile(pdfFormat, storedPdf);
                }
                if (hasEpubUpdate) {
                    StoredFile storedEpub = fileStorageService.storeFile(newEpubFile, book.getTitle(), "EPUB");
                    rollbackKeys.add(storedEpub.objectKey());
                    BookFormat epubFormat = getOrCreateFormat(book, "EPUB");
                    applyStoredFile(epubFormat, storedEpub);
                }

            } catch (Exception e) {
                rollbackKeys.forEach(fileStorageService::deleteFile);
                throw new RuntimeException("Update failed: " + e.getMessage(), e);
            }
        }

        Book saved = bookRepository.save(book);
        return mapToBookResponse(saved);
    }

    private void validateRequiredFormatFile(MultipartFile file, String formatLabel, String expectedExtension) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(formatLabel + " file is required");
        }
        validateFormatFile(file, formatLabel, expectedExtension);
    }

    private void validateFormatFile(MultipartFile file, String formatLabel, String expectedExtension) {
        if (file == null || file.isEmpty()) {
            return;
        }
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new IllegalArgumentException(formatLabel + " file name is required");
        }
        if (!originalFilename.toLowerCase(Locale.ROOT).endsWith("." + expectedExtension)) {
            throw new IllegalArgumentException(formatLabel + " file must be a ." + expectedExtension + " file");
        }
        if (!fileStorageService.isValidBookFile(file)) {
            throw new IllegalArgumentException("Invalid file type for " + originalFilename);
        }
    }

    private BookFormat getOrCreateFormat(Book book, String formatType) {
        return book.getFormats().stream()
                .filter(f -> f.getType().getName().equalsIgnoreCase(formatType))
                .findFirst()
                .orElseGet(() -> {
                    BookFormat newFormat = new BookFormat();
                    newFormat.setBook(book);
                    newFormat.setType(getOrCreateBookType(formatType));
                    book.getFormats().add(newFormat);
                    return newFormat;
                });
    }

    private void applyStoredFile(BookFormat target, StoredFile storedFile) {
        target.setContentUrl(storedFile.objectKey());
        target.setFileSizeKb(fileStorageService.calculateFileSizeKb(storedFile.sizeBytes()));
        target.setTotalPages(storedFile.totalPages());
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

    @Override
    public BookFileDownload getBookFileForDownload(Long bookId, Long formatId) {
        Book book = findBookOrThrow(bookId);

        BookFormat format = book.getFormats().stream()
                .filter(f -> f.getId().equals(formatId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Format not found with id: " + formatId));

        String objectKey = format.getContentUrl();

        DownloadedFile file = fileStorageService.getFile(objectKey);

        String fileName = buildDownloadFileName(book.getTitle(), format, objectKey);

        return BookFileDownload.builder()
                .inputStream(file.inputStream())
                .fileName(fileName)
                .contentType(file.resolvedContentType())
                .contentLength(file.sizeBytes())
                .build();
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
                    String downloadUrl = null;
                    if (StringUtils.hasText(format.getContentUrl())) {
                        try {
                            downloadUrl = fileStorageService.generatePresignedUrl(format.getContentUrl());
                        } catch (Exception ex) {
                            log.warn("Failed to generate presigned URL for object {}: {}", format.getContentUrl(), ex.getMessage());
                            downloadUrl = format.getContentUrl();
                        }
                    }
                    return new BookResponse.FormatInfo(
                            format.getId(),
                            format.getType().getName(),
                            format.getTotalPages(),
                            format.getFileSizeKb(),
                            downloadUrl,
                            downloadUrl);
                })
                .collect(java.util.stream.Collectors.toList());
        response.setFormats(formats);

        return response;
    }

    private Book findBookOrThrow(Long id) {
        return bookRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    private String buildDownloadFileName(String title, BookFormat format, String objectKey) {
        String baseName = sanitizeFileName(title);
        String extension = resolveFileExtension(format, objectKey);
        return baseName + "." + extension;
    }

    private String resolveFileExtension(BookFormat format, String objectKey) {
        String extensionFromKey = StringUtils.getFilenameExtension(objectKey);
        if (StringUtils.hasText(extensionFromKey)) {
            return extensionFromKey.toLowerCase(Locale.ROOT);
        }
        String typeName = Optional.ofNullable(format.getType())
                .map(BookType::getName)
                .orElse(null);
        if (StringUtils.hasText(typeName)) {
            return typeName.toLowerCase(Locale.ROOT);
        }
        return "bin";
    }

    private String sanitizeFileName(String title) {
        if (!StringUtils.hasText(title)) {
            return "book";
        }
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        String sanitized = normalized
                .replaceAll("[^\\w\\d-]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        if (!StringUtils.hasText(sanitized)) {
            return "book";
        }
        return sanitized.toLowerCase(Locale.ROOT);
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
