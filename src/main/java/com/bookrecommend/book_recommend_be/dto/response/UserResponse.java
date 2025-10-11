package com.bookrecommend.book_recommend_be.dto.response;

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
    private boolean activate;
    private String fullName;
    private boolean ban;
    private Instant createdAt;
    private Instant updatedAt;
    private String roleName;
}
