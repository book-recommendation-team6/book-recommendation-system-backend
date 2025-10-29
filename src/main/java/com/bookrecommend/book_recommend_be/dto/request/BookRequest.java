package com.bookrecommend.book_recommend_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class BookRequest {
    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    private String description;

    private Integer publicationYear;

    @Size(max = 100)
    private String publisher;

    @NotEmpty(message = "Book must have at least one author")
    private List<String> authorNames;

    @NotEmpty(message = "Book must have at least one genre")
    private List<Long> genreIds;

    private MultipartFile cover;
    private MultipartFile pdfFile;
    private MultipartFile epubFile;
}
