package com.bookrecommend.book_recommend_be.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UploadUrlRequest(
        @NotBlank String filename,
        @NotBlank String contentType   // "application/pdf"
) {}
