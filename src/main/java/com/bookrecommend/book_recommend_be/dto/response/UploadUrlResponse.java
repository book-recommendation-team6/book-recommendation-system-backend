package com.bookrecommend.book_recommend_be.dto.response;

public record UploadUrlResponse(
        String url,
        String key,
        String bucket,
        long   expiresInSeconds
) {}