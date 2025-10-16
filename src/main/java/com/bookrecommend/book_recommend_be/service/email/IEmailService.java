package com.bookrecommend.book_recommend_be.service.email;

public interface IEmailService {
    void sendVerificationEmail(String to, String username, String verificationLink);

    void sendResetPasswordEmail(String to, String username, String resetLink);
}
