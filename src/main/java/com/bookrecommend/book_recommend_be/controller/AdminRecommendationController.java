package com.bookrecommend.book_recommend_be.controller;

import com.bookrecommend.book_recommend_be.dto.recommendation.RecommendationModelInfo;
import com.bookrecommend.book_recommend_be.dto.recommendation.RecommendationModelsResponse;
import com.bookrecommend.book_recommend_be.dto.response.ApiResponse;
import com.bookrecommend.book_recommend_be.service.recommendation.RecsysRoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/admin/recommendation")
@RequiredArgsConstructor
public class AdminRecommendationController {

    private final RecsysRoutingService recsysRoutingService;

    @GetMapping("/models")
    public ResponseEntity<ApiResponse<RecommendationModelsResponse>> getModels() {
        RecommendationModelsResponse payload = new RecommendationModelsResponse(
                recsysRoutingService.getActiveModelKey(),
                recsysRoutingService.getAvailableModels()
        );
        return ResponseEntity.ok(
                ApiResponse.success(payload, "Recommendation model registry retrieved successfully")
        );
    }

    @PutMapping("/models/{modelKey}")
    public ResponseEntity<ApiResponse<RecommendationModelInfo>> activateModel(@PathVariable String modelKey) {
        try {
            RecommendationModelInfo info = recsysRoutingService.activateModel(modelKey);
            return ResponseEntity.ok(
                    ApiResponse.success(info, "Active recommendation model updated successfully")
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(ex.getMessage()));
        }
    }
}
