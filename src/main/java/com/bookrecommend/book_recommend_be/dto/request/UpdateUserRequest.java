package com.bookrecommend.book_recommend_be.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(max = 50)
    private String username;

    @Size(max = 15)
    private String phoneNumber;

    @Size(max = 255)
    private String avatarUrl;

    @Size(max = 100)
    private String fullName;
}
