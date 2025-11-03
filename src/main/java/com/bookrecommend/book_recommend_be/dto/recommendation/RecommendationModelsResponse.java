package com.bookrecommend.book_recommend_be.dto.recommendation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationModelsResponse {
    private String activeKey;
    private List<RecommendationModelInfo> models;
}
