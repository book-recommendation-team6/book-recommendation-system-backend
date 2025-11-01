package com.bookrecommend.book_recommend_be.controller;

import com.bookrecommend.book_recommend_be.dto.response.ApiResponse;
import com.bookrecommend.book_recommend_be.dto.response.BookResponse;
import com.bookrecommend.book_recommend_be.service.recommendation.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getRecommendations(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") int limit) {

        List<BookResponse> books = recommendationService.getRecommendations(userId, limit);
        return ResponseEntity.ok(
                ApiResponse.success(books, "Recommendations retrieved successfully")
        );
    }

    @GetMapping("/similar-books")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getSimilarBooks(
            @RequestParam Long bookId,
            @RequestParam(defaultValue = "10") int limit) {

        // Validate limit
        if (limit < 1 || limit > 100) {
            List<BookResponse> emptyList = Collections.emptyList();
            return ResponseEntity.ok(
                    ApiResponse.success(emptyList, "Limit must be between 1 and 100")
            );
        }

        List<BookResponse> books = recommendationService.getSimilarBooks(bookId, limit);
        return ResponseEntity.ok(
                ApiResponse.success(books, "Similar books retrieved successfully")
        );
    }


}