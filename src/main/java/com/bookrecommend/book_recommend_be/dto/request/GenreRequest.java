package com.bookrecommend.book_recommend_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GenreRequest {

    @NotBlank
    @Size(max = 50)
    private String name;

    @Size(max = 500)
    private String description;
}
