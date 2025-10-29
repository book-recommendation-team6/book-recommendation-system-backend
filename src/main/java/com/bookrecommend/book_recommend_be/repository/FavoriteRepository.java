package com.bookrecommend.book_recommend_be.repository;

import com.bookrecommend.book_recommend_be.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    @Query("""
            SELECT f FROM Favorite f
            JOIN f.book b
            WHERE f.user.id = :userId
              AND f.book.id = :bookId
              AND b.isDeleted = false
            """)
    Optional<Favorite> findByUserIdAndBookId(@Param("userId") Long userId,
                                             @Param("bookId") Long bookId);

    @Query("""
            SELECT f FROM Favorite f
            JOIN f.book b
            WHERE f.user.id = :userId
              AND b.isDeleted = false
            ORDER BY f.addedAt DESC
            """)
    List<Favorite> findAllByUserIdOrderByAddedAtDesc(@Param("userId") Long userId);
}
