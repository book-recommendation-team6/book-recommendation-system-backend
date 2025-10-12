package com.bookrecommend.book_recommend_be.controller;

import com.bookrecommend.book_recommend_be.dto.response.ApiResponse;
import com.bookrecommend.book_recommend_be.dto.response.FavoriteResponse;
import com.bookrecommend.book_recommend_be.service.favorite.IFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/users/{userId}")
@RequiredArgsConstructor
public class FavoriteController {

    private final IFavoriteService favoriteService;

    @PostMapping("/favorites/{bookId}")
    public ResponseEntity<ApiResponse<FavoriteResponse>> addFavorite(@PathVariable Long userId,
                                                                     @PathVariable Long bookId) {
        FavoriteResponse response = favoriteService.addFavorite(userId, bookId);
        return ResponseEntity.ok(ApiResponse.success(response, "Book added to favorites"));
    }

    @DeleteMapping("/favorites/{bookId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(@PathVariable Long userId,
                                                            @PathVariable Long bookId) {
        favoriteService.removeFavorite(userId, bookId);
        return ResponseEntity.ok(ApiResponse.success(null, "Book removed from favorites"));
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getUserFavorites(@PathVariable Long userId) {
        List<FavoriteResponse> favorites = favoriteService.getUserFavorites(userId);
        return ResponseEntity.ok(ApiResponse.success(favorites, "Favorites fetched successfully"));
    }
}
