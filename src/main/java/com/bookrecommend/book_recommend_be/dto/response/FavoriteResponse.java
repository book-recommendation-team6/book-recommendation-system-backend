package com.bookrecommend.book_recommend_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {
    Long id;
    Long userId;
    Long bookId;
    Instant addedAt;
    BookInfo book;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookInfo {
        Long id;
        String title;
        String description;
        String coverImageUrl;
        Integer publicationYear;
        String publisher;
        Set<AuthorInfo> authors;
        Set<GenreInfo> genres;
        List<FormatInfo> formats;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {
        Long id;
        String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenreInfo {
        Long id;
        String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormatInfo {
        Long id;
        String typeName;
        Integer totalPages;
        Integer fileSizeKb;
        String contentUrl;
    }
}
