package com.bookrecommend.book_recommend_be.dto.recommendation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimilarBooksResponse {
    @JsonProperty("book_id")
    private Long bookId;

    private List<SimilarItem> items;
}

