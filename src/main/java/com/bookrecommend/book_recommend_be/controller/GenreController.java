package com.bookrecommend.book_recommend_be.controller;

import com.bookrecommend.book_recommend_be.dto.response.ApiResponse;
import com.bookrecommend.book_recommend_be.dto.response.GenreResponse;
import com.bookrecommend.book_recommend_be.service.genre.IGenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/books/genres")
@RequiredArgsConstructor
public class GenreController {

    private final IGenreService genreService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GenreResponse>>> getGenres(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<GenreResponse> genres = genreService.getGenres(page, size);
        return ResponseEntity.ok(ApiResponse.success(genres, "Genres retrieved successfully"));
    }
}
