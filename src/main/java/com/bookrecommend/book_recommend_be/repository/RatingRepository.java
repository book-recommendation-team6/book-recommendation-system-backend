package com.bookrecommend.book_recommend_be.repository;

import com.bookrecommend.book_recommend_be.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserIdAndBookId(Long userId, Long bookId);

    List<Rating> findAllByUserId(Long userId);
}
