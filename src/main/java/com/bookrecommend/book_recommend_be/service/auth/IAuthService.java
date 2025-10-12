package com.bookrecommend.book_recommend_be.service.auth;

import com.bookrecommend.book_recommend_be.dto.request.LoginRequest;
import com.bookrecommend.book_recommend_be.dto.request.RegisterRequest;
import com.bookrecommend.book_recommend_be.dto.request.ResetPasswordRequest;
import com.bookrecommend.book_recommend_be.dto.response.LoginResponse;

public interface IAuthService {
    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);

    void verifyEmail(String token);

    void forgotPassword(String email);

    void resetPassword(ResetPasswordRequest request);
}
