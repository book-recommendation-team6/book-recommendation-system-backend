package com.bookrecommend.book_recommend_be.repository;

import com.bookrecommend.book_recommend_be.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserIdAndBookId(Long userId, Long bookId);

    List<Rating> findAllByUserId(Long userId);

    List<Rating> findAllByBookId(Long bookId);

    @Query("SELECT AVG(r.value) FROM Rating r WHERE r.book.id = :bookId")
    Double findAverageRatingByBookId(@Param("bookId") Long bookId);
}
