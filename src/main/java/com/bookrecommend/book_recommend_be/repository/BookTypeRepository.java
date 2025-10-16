package com.bookrecommend.book_recommend_be.repository;

import com.bookrecommend.book_recommend_be.model.BookType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookTypeRepository extends JpaRepository<BookType, Long> {
    Optional<BookType> findByName(String name);
}
