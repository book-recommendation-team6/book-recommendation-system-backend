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
    public RatingResponse rateBook(Long userId, Long bookId, RatingRequest request) {
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

        Rating savedRating = ratingRepository.save(rating);
        return mapToRatingResponse(savedRating);
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
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    private RatingResponse mapToRatingResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .userId(rating.getUser().getId())
                .bookId(rating.getBook().getId())
                .value(rating.getValue())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}
