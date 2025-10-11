package com.bookrecommend.book_recommend_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BookResponse {
    Long id;
    String title;
    String description;
    String coverImageUrl;
    Integer publicationYear;
    String publisher;
    Instant createdAt;
    Instant updatedAt;
}
