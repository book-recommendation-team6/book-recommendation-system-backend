package com.bookrecommend.book_recommend_be.service.book;

import lombok.Builder;

import java.io.InputStream;

@Builder
public record BookFileDownload(
        InputStream inputStream,
        String fileName,
        String contentType,
        long contentLength
) {
    public String resolvedContentType() {
        return (contentType == null || contentType.isBlank())
                ? "application/octet-stream"
                : contentType;
    }
}
