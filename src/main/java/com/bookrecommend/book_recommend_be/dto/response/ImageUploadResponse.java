package com.bookrecommend.book_recommend_be.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ImageUploadResponse {
    String url;
    String secureUrl;
    String publicId;
}
