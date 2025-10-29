package com.bookrecommend.book_recommend_be.controller;

import com.bookrecommend.book_recommend_be.dto.request.RatingRequest;
import com.bookrecommend.book_recommend_be.dto.response.ApiResponse;
import com.bookrecommend.book_recommend_be.dto.response.RatingResponse;
import com.bookrecommend.book_recommend_be.service.rating.IRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/users/{userId}")
@RequiredArgsConstructor
public class RatingController {

    private final IRatingService ratingService;

    @PostMapping("/books/{bookId}/ratings")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> rateBook(@PathVariable Long userId,
                                                                       @PathVariable Long bookId,
                                                                       @Valid @RequestBody RatingRequest request) {
        List<RatingResponse> ratings = ratingService.rateBook(userId, bookId, request);
        return ResponseEntity.ok(ApiResponse.success(ratings, "Rating saved successfully"));
    }

    @DeleteMapping("/books/{bookId}/ratings")
    public ResponseEntity<ApiResponse<Void>> deleteRating(@PathVariable Long userId,
                                                          @PathVariable Long bookId) {
        ratingService.deleteRating(userId, bookId);
        return ResponseEntity.ok(ApiResponse.success(null, "Rating removed successfully"));
    }

    @GetMapping("/ratings")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getUserRatings(@PathVariable Long userId) {
        List<RatingResponse> ratings = ratingService.getUserRatings(userId);
        return ResponseEntity.ok(ApiResponse.success(ratings, "Ratings fetched successfully"));
    }

    @GetMapping("/books/{bookId}/ratings")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getBookRatings(@PathVariable Long userId,
                                                                              @PathVariable Long bookId) {
        List<RatingResponse> ratings = ratingService.getBookRatings(userId, bookId);
        String message = ratings.isEmpty() ?
                "Chưa có đánh giá nào cho sách này" :
                "Lấy danh sách đánh giá thành công";
        return ResponseEntity.ok(ApiResponse.success(ratings, message));
    }

    @GetMapping("/books/{bookId}/average-rating")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(@PathVariable Long bookId) {
        Double average = ratingService.getAverageRatingByBookId(bookId);
        return ResponseEntity.ok(ApiResponse.success(average, "Lấy điểm đánh giá trung bình thành công"));
    }
}
