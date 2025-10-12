package com.bookrecommend.book_recommend_be.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookmarkRequest {
    @Positive
    private Integer pageNumber;

    @Size(max = 255)
    private String locationInBook;
}
