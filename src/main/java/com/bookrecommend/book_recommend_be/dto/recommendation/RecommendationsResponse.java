package com.bookrecommend.book_recommend_be.dto.recommendation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationsResponse {
    @JsonProperty("user_id")
    private Long userId;

    private Integer limit;

    private List<RecommendationItem> items;
}