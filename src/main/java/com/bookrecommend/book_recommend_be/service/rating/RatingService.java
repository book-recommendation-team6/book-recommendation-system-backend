package com.bookrecommend.book_recommend_be.service.rating;

import com.bookrecommend.book_recommend_be.dto.request.RatingRequest;
import com.bookrecommend.book_recommend_be.dto.response.RatingResponse;
import com.bookrecommend.book_recommend_be.exceptions.ResourceNotFoundException;
import com.bookrecommend.book_recommend_be.model.Book;
import com.bookrecommend.book_recommend_be.model.Rating;
import com.bookrecommend.book_recommend_be.model.User;
import com.bookrecommend.book_recommend_be.repository.BookRepository;
import com.bookrecommend.book_recommend_be.repository.RatingRepository;
import com.bookrecommend.book_recommend_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService implements IRatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public List<RatingResponse> rateBook(Long userId, Long bookId, RatingRequest request) {
        User user = getUserOrThrow(userId);
        Book book = getBookOrThrow(bookId);

        Rating rating = ratingRepository.findByUserIdAndBookId(userId, bookId)
                .map(existing -> {
                    existing.setValue(request.getValue());
                    existing.setComment(request.getComment());
                    return existing;
                })
                .orElseGet(() -> Rating.builder()
                        .user(user)
                        .book(book)
                        .value(request.getValue())
                        .comment(request.getComment())
                        .build());

        ratingRepository.save(rating);

        return ratingRepository.findAllByBookId(bookId).stream()
                .map(this::mapToRatingResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteRating(Long userId, Long bookId) {
        Rating rating = ratingRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found for user and book"));
        ratingRepository.delete(rating);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponse> getUserRatings(Long userId) {
        ensureUserExists(userId);
        return ratingRepository.findAllByUserId(userId).stream()
                .map(this::mapToRatingResponse)
                .toList();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private Book getBookOrThrow(Long bookId) {
        return bookRepository.findByIdAndIsDeletedFalse(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponse> getBookRatings(Long userId, Long bookId) {
        // Kiểm tra sách tồn tại
        if (!bookRepository.existsByIdAndIsDeletedFalse(bookId)) {
            throw new ResourceNotFoundException("Không tìm thấy sách với ID: " + bookId);
        }

        // Nếu userId = 0, lấy tất cả ratings của sách
        if (userId == 0) {
            return ratingRepository.findAllByBookId(bookId).stream()
                    .map(this::mapToRatingResponse)
                    .toList();
        }

        // Nếu userId != 0, lấy rating của user cụ thể
        return ratingRepository.findByUserIdAndBookId(userId, bookId)
                .map(rating -> List.of(mapToRatingResponse(rating)))
                .orElse(List.of());
    }

    private RatingResponse mapToRatingResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .userId(rating.getUser().getId())
                .userName(rating.getUser().getUsername())
                .bookId(rating.getBook().getId())
                .value(rating.getValue())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingByBookId(Long bookId) {
        if (!bookRepository.existsByIdAndIsDeletedFalse(bookId)) {
            throw new ResourceNotFoundException("Không tìm thấy sách với ID: " + bookId);
        }
        Double average = ratingRepository.findAverageRatingByBookId(bookId);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }
}
