package com.bookrecommend.book_recommend_be.controller;

import com.bookrecommend.book_recommend_be.dto.request.ReadingHistoryRequest;
import com.bookrecommend.book_recommend_be.dto.response.ApiResponse;
import com.bookrecommend.book_recommend_be.dto.response.ReadingHistoryResponse;
import com.bookrecommend.book_recommend_be.service.history.IReadingHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/users/{userId}")
@RequiredArgsConstructor
public class ReadingHistoryController {

    private final IReadingHistoryService readingHistoryService;

    @PostMapping("/books/{bookId}/history")
    public ResponseEntity<ApiResponse<ReadingHistoryResponse>> recordHistory(@PathVariable Long userId,
                                                                             @PathVariable Long bookId,
                                                                             @Valid @RequestBody ReadingHistoryRequest request) {
        ReadingHistoryResponse response = readingHistoryService.recordReadingHistory(userId, bookId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Reading history updated successfully"));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<ReadingHistoryResponse>>> getUserHistory(@PathVariable Long userId,
                                                                                    @RequestParam(defaultValue = "0") int page,
                                                                                    @RequestParam(defaultValue = "10") int size) {
        Page<ReadingHistoryResponse> history = readingHistoryService.getUserReadingHistory(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(history, "Reading history fetched successfully"));
    }
}
