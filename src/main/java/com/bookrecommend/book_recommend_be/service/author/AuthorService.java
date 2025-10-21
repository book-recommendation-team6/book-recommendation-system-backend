package com.bookrecommend.book_recommend_be.service.author;

import com.bookrecommend.book_recommend_be.dto.response.AuthorResponse;

import java.util.List;

public interface AuthorService {
    List<AuthorResponse> getAllAuthors();
}