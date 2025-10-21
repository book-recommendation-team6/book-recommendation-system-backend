package com.bookrecommend.book_recommend_be.controller;

import com.bookrecommend.book_recommend_be.dto.request.BookRequest;
import com.bookrecommend.book_recommend_be.dto.response.ApiResponse;
import com.bookrecommend.book_recommend_be.dto.response.BookResponse;
import com.bookrecommend.book_recommend_be.dto.response.ImageUploadResponse;
import com.bookrecommend.book_recommend_be.service.book.IBookService;
import com.bookrecommend.book_recommend_be.service.file.IFileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/books")
@RequiredArgsConstructor
public class BookController {

    private final IBookService bookService;
    private final IFileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookResponse> books = bookService.getBooks(page, size);
        return ResponseEntity.ok(ApiResponse.success(books, "Books retrieved successfully"));
    }

    @GetMapping("/newest")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getNewestBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookResponse> books = bookService.getNewestBooks(page, size);
        return ResponseEntity.ok(ApiResponse.success(books, "Newest books retrieved successfully"));
    }

    @GetMapping("/most-read")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getMostReadBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookResponse> books = bookService.getMostReadBooks(page, size);
        return ResponseEntity.ok(ApiResponse.success(books, "Most read books retrieved successfully"));
    }

    @GetMapping("/genre/{genreId}")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getBooksByGenre(
            @PathVariable Long genreId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookResponse> books = bookService.getBooksByGenre(genreId, page, size);
        return ResponseEntity.ok(ApiResponse.success(books, "Books by genre retrieved successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> searchBooks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BookResponse> books = bookService.searchBooks(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(books, "Books found successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(@PathVariable Long id) {
        BookResponse book = bookService.getBookById(id);
        return ResponseEntity.ok(ApiResponse.success(book, "Book retrieved successfully"));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<BookResponse>> createBook(@Valid @RequestBody BookRequest request) {
        BookResponse createdBook = bookService.createBook(request);
        return ResponseEntity
                .created(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(createdBook.getId())
                        .toUri())
                .body(ApiResponse.success(createdBook, "Book created successfully"));
    }
    
    @PostMapping(value = "/create-with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BookResponse>> createBookWithFiles(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("coverImageUrl") String coverImageUrl,
            @RequestParam(value = "publicationYear", required = false) Integer publicationYear,
            @RequestParam(value = "publisher", required = false) String publisher,
            @RequestParam("authorNames") List<String> authorNames,
            @RequestParam("genreIds") List<Long> genreIds,
            @RequestPart("file") MultipartFile file) {
        
        BookResponse createdBook = bookService.createBookWithFiles(
                title, description, coverImageUrl, publicationYear, publisher,
                authorNames, genreIds, file);
        
        return ResponseEntity
                .created(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/../{id}")
                        .buildAndExpand(createdBook.getId())
                        .toUri())
                .body(ApiResponse.success(createdBook, "Book created with files successfully"));
    }

    @PostMapping(value = "/cover/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadBookCover(
            @RequestPart("cover") MultipartFile coverFile) {
        ImageUploadResponse response = bookService.uploadCoverImage(coverFile);
        return ResponseEntity.ok(ApiResponse.success(response, "Book cover uploaded successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(@PathVariable Long id,
                                                                @Valid @RequestBody BookRequest request) {
        BookResponse updatedBook = bookService.updateBook(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedBook, "Book updated successfully"));
    }
    
    @PutMapping(value = "/update-with-files/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BookResponse>> updateBookWithFiles(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("coverImageUrl") String coverImageUrl,
            @RequestParam(value = "publicationYear", required = false) Integer publicationYear,
            @RequestParam(value = "publisher", required = false) String publisher,
            @RequestParam("authorNames") List<String> authorNames,
            @RequestParam("genreIds") List<Long> genreIds,
            @RequestPart("file") MultipartFile file) {
        
        BookResponse updatedBook = bookService.updateBookWithFiles(
                id, title, description, coverImageUrl, publicationYear, publisher,
                authorNames, genreIds, file);
        
        return ResponseEntity.ok(ApiResponse.success(updatedBook, "Book updated with files successfully"));
    }

    @PatchMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BookResponse>> updateBookCover(
            @PathVariable Long id,
            @RequestPart("cover") MultipartFile coverFile) {
        BookResponse updatedBook = bookService.updateCoverImage(id, coverFile);
        return ResponseEntity.ok(ApiResponse.success(updatedBook, "Book cover updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Book deleted successfully"));
    }
    
    @GetMapping("/{bookId}/download/{formatId}")
    public ResponseEntity<Resource> downloadBookFile(
            @PathVariable Long bookId,
            @PathVariable Long formatId,
            HttpServletRequest request) {
        
        // Get format by calling service method that will be added
        String fileUrl = bookService.getBookFormatUrl(bookId, formatId);
        
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileUrl);
        
        // Determine content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (Exception e) {
            contentType = "application/octet-stream";
        }
        
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
