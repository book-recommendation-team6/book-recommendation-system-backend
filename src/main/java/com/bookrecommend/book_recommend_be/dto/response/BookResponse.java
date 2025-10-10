package com.bookrecommend.book_recommend_be.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
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
