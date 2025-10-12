package com.bookrecommend.book_recommend_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class FavoriteResponse {
    Long id;
    Long userId;
    Long bookId;
    Instant addedAt;
}
