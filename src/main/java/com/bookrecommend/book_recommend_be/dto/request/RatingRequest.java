package com.bookrecommend.book_recommend_be.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RatingRequest {
    @NotNull
    @Min(1)
    @Max(5)
    private Integer value;

    @Size(max = 1000)
    private String comment;
}
