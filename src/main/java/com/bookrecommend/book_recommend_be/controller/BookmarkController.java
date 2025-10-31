package com.bookrecommend.book_recommend_be.controller;

import com.bookrecommend.book_recommend_be.dto.request.BookmarkRequest;
import com.bookrecommend.book_recommend_be.dto.response.ApiResponse;
import com.bookrecommend.book_recommend_be.dto.response.BookmarkResponse;
import com.bookrecommend.book_recommend_be.service.bookmark.IBookmarkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/users/{userId}")
@RequiredArgsConstructor
public class BookmarkController {

    private final IBookmarkService bookmarkService;

    @PostMapping("/books/{bookId}/bookmarks")
    public ResponseEntity<ApiResponse<BookmarkResponse>> createBookmark(
            @PathVariable Long userId,
            @PathVariable Long bookId,
            @Valid @RequestBody BookmarkRequest request) {
        BookmarkResponse response = bookmarkService.createBookmark(userId, bookId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Bookmark created successfully"));
    }

    @PutMapping("/bookmarks/{bookmarkId}")
    public ResponseEntity<ApiResponse<BookmarkResponse>> updateBookmark(
            @PathVariable Long userId,
            @PathVariable Long bookmarkId,
            @Valid @RequestBody BookmarkRequest request) {
        BookmarkResponse response = bookmarkService.updateBookmark(userId, bookmarkId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Bookmark updated successfully"));
    }

    @DeleteMapping("/bookmarks/{bookmarkId}")
    public ResponseEntity<ApiResponse<Void>> removeBookmark(
            @PathVariable Long userId,
            @PathVariable Long bookmarkId) {
        bookmarkService.deleteBookmark(userId, bookmarkId);
        return ResponseEntity.ok(ApiResponse.success(null, "Bookmark deleted successfully"));
    }

    @GetMapping("/books/{bookId}/bookmarks")
    public ResponseEntity<ApiResponse<List<BookmarkResponse>>> getBookmarksForBook(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        List<BookmarkResponse> bookmarks = bookmarkService.getBookmarksForBook(userId, bookId);
        return ResponseEntity.ok(ApiResponse.success(bookmarks, "Bookmarks fetched successfully"));
    }
}
