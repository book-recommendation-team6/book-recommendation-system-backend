package com.bookrecommend.book_recommend_be.controller;

import com.bookrecommend.book_recommend_be.dto.request.GenreRequest;
import com.bookrecommend.book_recommend_be.dto.response.ApiResponse;
import com.bookrecommend.book_recommend_be.dto.response.GenreResponse;
import com.bookrecommend.book_recommend_be.service.genre.IGenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/admin/genres")
@RequiredArgsConstructor
public class AdminGenreController {

    private final IGenreService genreService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<GenreResponse>> createGenre(@Valid @RequestBody GenreRequest request) {
        GenreResponse created = genreService.createGenre(request);
        return ResponseEntity.ok(ApiResponse.success(created, "Genre created successfully"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<GenreResponse>> updateGenre(@PathVariable Long id,
                                                                  @Valid @RequestBody GenreRequest request) {
        GenreResponse updated = genreService.updateGenre(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Genre updated successfully"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGenre(@PathVariable Long id) {
        genreService.deleteGenre(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Genre deleted successfully"));
    }
}
