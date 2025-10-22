package com.bookrecommend.book_recommend_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class GenreResponse {
    private Long id;
    private String name;
    private String description;
}
