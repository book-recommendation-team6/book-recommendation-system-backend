package com.bookrecommend.book_recommend_be.service.rating;

import com.bookrecommend.book_recommend_be.dto.request.RatingRequest;
import com.bookrecommend.book_recommend_be.dto.response.RatingResponse;

import java.util.List;

public interface IRatingService {

    RatingResponse rateBook(Long userId, Long bookId, RatingRequest request);

    void deleteRating(Long userId, Long bookId);

    List<RatingResponse> getUserRatings(Long userId);
}