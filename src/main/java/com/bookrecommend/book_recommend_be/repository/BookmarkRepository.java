package com.bookrecommend.book_recommend_be.repository;

import com.bookrecommend.book_recommend_be.model.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findAllByUserIdAndBookIdOrderByCreatedAtDesc(Long userId, Long bookId);
}
