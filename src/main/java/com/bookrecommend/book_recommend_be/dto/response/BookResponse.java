package com.bookrecommend.book_recommend_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Set;

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
    Set<AuthorInfo> authors;
    Set<GenreInfo> genres;
    List<FormatInfo> formats;
    
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class AuthorInfo {
        Long id;
        String name;
    }
    
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class GenreInfo {
        Long id;
        String name;
    }
    
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class FormatInfo {
        Long id;
        String typeName;
        Integer totalPages;
        Integer fileSizeKb;
        String contentUrl;
        String downloadUrl;
    }
}
