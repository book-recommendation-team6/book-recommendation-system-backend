package com.bookrecommend.book_recommend_be.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkIdsRequest {

    @NotEmpty(message = "At least one id must be provided")
    private List<Long> ids;
}
