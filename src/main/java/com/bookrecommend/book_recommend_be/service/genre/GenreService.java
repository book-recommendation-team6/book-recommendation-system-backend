package com.bookrecommend.book_recommend_be.service.genre;

import com.bookrecommend.book_recommend_be.dto.response.GenreResponse;

import java.util.List;

public interface GenreService {
    List<GenreResponse> getAllGenres();
}
