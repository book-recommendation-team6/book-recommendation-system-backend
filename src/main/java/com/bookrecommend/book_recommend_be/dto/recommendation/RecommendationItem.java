package com.bookrecommend.book_recommend_be.dto.recommendation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationItem {
    @JsonProperty("book_id")
    private Long bookId;

    private Double score;

    private String title;

    private String author;
}