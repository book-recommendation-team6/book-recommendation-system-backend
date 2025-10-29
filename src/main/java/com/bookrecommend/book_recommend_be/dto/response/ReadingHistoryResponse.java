package com.bookrecommend.book_recommend_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ReadingHistoryResponse {
    private Long id;
    private Long userId;
    private Long bookId;
    private Double progress;
    private Instant lastReadAt;
    private BookSummary book;

    @Data
    @Builder
    public static class BookSummary {
        private Long id;
        private String title;
        private String coverImageUrl;
        private List<AuthorSummary> authors;
    }

    @Data
    @Builder
    public static class AuthorSummary {
        private Long id;
        private String name;
    }
}
