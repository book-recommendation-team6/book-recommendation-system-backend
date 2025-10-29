package com.bookrecommend.book_recommend_be.dto.response;

import com.bookrecommend.book_recommend_be.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private String fullName;
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private String roleName;
}
