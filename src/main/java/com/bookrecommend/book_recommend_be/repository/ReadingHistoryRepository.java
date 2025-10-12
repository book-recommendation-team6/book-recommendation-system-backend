package com.bookrecommend.book_recommend_be.repository;

import com.bookrecommend.book_recommend_be.model.ReadingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {
    Optional<ReadingHistory> findByUserIdAndBookId(Long userId, Long bookId);

    List<ReadingHistory> findAllByUserIdOrderByLastReadAtDesc(Long userId);
}

