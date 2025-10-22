package com.bookrecommend.book_recommend_be.service.genre;

import com.bookrecommend.book_recommend_be.dto.response.GenreResponse;
import org.springframework.data.domain.Page;

public interface IGenreService {
    Page<GenreResponse> getGenres(int page, int size);
}
