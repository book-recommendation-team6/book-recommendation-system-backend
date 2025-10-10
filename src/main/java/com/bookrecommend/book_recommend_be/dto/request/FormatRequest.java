package com.bookrecommend.book_recommend_be.dto.request;

import lombok.Data;

@Data
public class FormatRequest {
    private Long typeId;
    private String contentUrl;
    private Integer totalPages;
    private Integer fileSizeKb;
}

