package com.bookrecommend.book_recommend_be.repository;

import com.bookrecommend.book_recommend_be.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    Page<Book> findByIsDeletedFalse(Pageable pageable);

    long countByIsDeletedFalse();

    boolean existsByIdAndIsDeletedFalse(Long id);

    Optional<Book> findByIdAndIsDeletedFalse(Long id);

    @Query("""
            SELECT b FROM Book b
            WHERE b.isDeleted = false
            ORDER BY b.createdAt DESC
            """)
    Page<Book> findNewestBooks(Pageable pageable);

    @Query("""
            SELECT b FROM Book b
            LEFT JOIN b.readingHistories rh
            WHERE b.isDeleted = false
            GROUP BY b
            ORDER BY COUNT(rh) DESC
            """)
    Page<Book> findMostReadBooks(Pageable pageable);

    @Query("""
            SELECT b FROM Book b
            WHERE b.isDeleted = false
              AND EXISTS (
                  SELECT 1 FROM b.genres g
                  WHERE g.id = :genreId
              )
            """)
    Page<Book> findBooksByGenre(Long genreId, Pageable pageable);

    @Query("""
            SELECT b FROM Book b
            JOIN b.genres g
            LEFT JOIN b.readingHistories rh
            WHERE g.id = :genreId
              AND b.isDeleted = false
            GROUP BY b
            ORDER BY COUNT(rh) DESC, b.title ASC
            """)
    Page<Book> findBooksByGenreOrderByPopularity(Long genreId, Pageable pageable);

    @Query("""
            SELECT DISTINCT b FROM Book b
            LEFT JOIN b.authors a
            WHERE b.isDeleted = false
              AND (
                    LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY b.createdAt DESC
            """)
    Page<Book> searchBooks(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT b.id AS bookId,
                   b.title AS title,
                   b.coverImageUrl AS coverImageUrl,
                   COALESCE(AVG(r.value), 0) AS averageRating,
                   COUNT(r) AS ratingCount
            FROM Book b
            LEFT JOIN b.ratings r
            WHERE b.isDeleted = false
            GROUP BY b.id, b.title, b.coverImageUrl
            ORDER BY COALESCE(AVG(r.value), 0) DESC, COUNT(r) DESC
            """)
    Page<Object[]> findTopRatedBooks(Pageable pageable);

    @Query("""
            SELECT b.id AS bookId,
                   b.title AS title,
                   b.coverImageUrl AS coverImageUrl,
                   COUNT(f) AS favoriteCount
            FROM Book b
            LEFT JOIN b.favorites f
            WHERE b.isDeleted = false
            GROUP BY b.id, b.title, b.coverImageUrl
            ORDER BY COUNT(f) DESC, b.title ASC
            """)
    Page<Object[]> findTopFavoritedBooks(Pageable pageable);
}
