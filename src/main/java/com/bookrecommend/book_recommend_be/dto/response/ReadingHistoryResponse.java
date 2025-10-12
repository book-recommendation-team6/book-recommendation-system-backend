package com.bookrecommend.book_recommend_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ReadingHistoryResponse {
    Long id;
    Long userId;
    Long bookId;
    Double progress;
    Instant lastReadAt;
}
