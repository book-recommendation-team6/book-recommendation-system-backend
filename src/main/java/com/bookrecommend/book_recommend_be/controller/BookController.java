package com.bookrecommend.book_recommend_be.controller;

import com.bookrecommend.book_recommend_be.dto.request.BookRequest;
import com.bookrecommend.book_recommend_be.dto.response.ApiResponse;
import com.bookrecommend.book_recommend_be.dto.response.BookResponse;
import com.bookrecommend.book_recommend_be.service.book.IBookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("${api.prefix}/books")
@RequiredArgsConstructor
public class BookController {

    private final IBookService bookService;

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

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            @ModelAttribute @Valid BookRequest request) {
        BookResponse created = bookService.createBook(request);
        return ResponseEntity
                .created(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(created.getId())
                        .toUri())
                .body(ApiResponse.success(created, "Book created successfully"));
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable Long id,
            @ModelAttribute @Valid BookRequest request) {
        BookResponse updated = bookService.updateBook(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Book updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Book deleted successfully"));
    }

    @GetMapping("/{bookId}/download/{formatId}")
    public ResponseEntity<Void> downloadBookFile(
            @PathVariable Long bookId,
            @PathVariable Long formatId) {

        String downloadUrl = bookService.getBookFormatUrl(bookId, formatId);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(downloadUrl))
                .build();
    }
}
