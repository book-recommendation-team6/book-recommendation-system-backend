package com.bookrecommend.book_recommend_be.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookRequest {
    private String title;
    private Long authorId;
    private String publisher;
    private Integer publishYear;
    private List<Long> genreIds;
    private String description;
    private MultipartFile coverImage;
    private MultipartFile bookFile;
    private String format; // PDF, EPUB, MOBI, etc.
}