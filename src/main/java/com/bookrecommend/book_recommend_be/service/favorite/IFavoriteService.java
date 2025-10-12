package com.bookrecommend.book_recommend_be.service.favorite;

import com.bookrecommend.book_recommend_be.dto.response.FavoriteResponse;

import java.util.List;

public interface IFavoriteService {

    FavoriteResponse addFavorite(Long userId, Long bookId);

    void removeFavorite(Long userId, Long bookId);

    List<FavoriteResponse> getUserFavorites(Long userId);
}
