package com.bookrecommend.book_recommend_be.service.file;

import lombok.Builder;

import java.io.InputStream;

@Builder
public record DownloadedFile(
        InputStream inputStream,
        String contentType,
        long sizeBytes
) {
    public String resolvedContentType() {
        return contentType == null || contentType.isBlank()
                ? "application/octet-stream"
                : contentType;
    }
}
