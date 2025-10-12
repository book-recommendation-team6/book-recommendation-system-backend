package com.bookrecommend.book_recommend_be.service.email;

public interface IEmailService {
    void sendEmail(String to, String subject, String content);
}