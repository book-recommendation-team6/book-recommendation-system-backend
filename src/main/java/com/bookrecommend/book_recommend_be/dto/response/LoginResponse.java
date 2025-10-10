package com.bookrecommend.book_recommend_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String jwt;
    private Long id;
    private String username;
    private String email;
    private String role;
}

