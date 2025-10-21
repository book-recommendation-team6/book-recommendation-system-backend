package com.bookrecommend.book_recommend_be.service.file;

import lombok.Builder;

@Builder
public record StoredFile(
        String objectKey,
        String fileName,
        long sizeBytes,
        String contentType,
        Integer totalPages
) {
    public int sizeInKb() {
        if (sizeBytes == 0) {
            return 0;
        }
        return (int) Math.max(1, Math.ceil(sizeBytes / 1024.0));
    }
}
