package com.bookrecommend.book_recommend_be.repository;

import com.bookrecommend.book_recommend_be.model.ReadingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {
    @Query("""
            SELECT rh FROM ReadingHistory rh
            JOIN rh.book b
            WHERE rh.user.id = :userId
              AND rh.book.id = :bookId
              AND b.isDeleted = false
            """)
    Optional<ReadingHistory> findByUserIdAndBookId(@Param("userId") Long userId,
                                                   @Param("bookId") Long bookId);

    @Query("""
            SELECT rh FROM ReadingHistory rh
            JOIN rh.book b
            WHERE rh.user.id = :userId
              AND b.isDeleted = false
            """)
    Page<ReadingHistory> findAllByUserId(@Param("userId") Long userId, Pageable pageable);
}
