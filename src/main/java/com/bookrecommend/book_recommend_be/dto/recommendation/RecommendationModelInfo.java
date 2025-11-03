package com.bookrecommend.book_recommend_be.dto.recommendation;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RecommendationModelInfo {
    private String key;
    private String label;
    private String baseUrl;
    private boolean supportsOnlineLearning;
    private boolean active;
}
