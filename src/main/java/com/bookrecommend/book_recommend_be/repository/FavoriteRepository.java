package com.bookrecommend.book_recommend_be.repository;

import com.bookrecommend.book_recommend_be.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserIdAndBookId(Long userId, Long bookId);

    List<Favorite> findAllByUserIdOrderByAddedAtDesc(Long userId);
}
