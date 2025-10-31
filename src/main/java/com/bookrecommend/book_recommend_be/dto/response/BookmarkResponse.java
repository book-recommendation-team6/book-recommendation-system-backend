package com.bookrecommend.book_recommend_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BookmarkResponse {
    Long id;
    Long userId;
    Long bookId;
    Integer pageNumber;
    String locationInBook;
    String note;
    Instant createdAt;
}
