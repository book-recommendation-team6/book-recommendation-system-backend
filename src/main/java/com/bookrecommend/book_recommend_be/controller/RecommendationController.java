package com.bookrecommend.book_recommend_be.controller;

import com.bookrecommend.book_recommend_be.dto.response.ApiResponse;
import com.bookrecommend.book_recommend_be.dto.response.BookResponse;
import com.bookrecommend.book_recommend_be.service.recommendation.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}