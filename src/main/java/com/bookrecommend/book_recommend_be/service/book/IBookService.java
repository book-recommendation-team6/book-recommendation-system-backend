package com.bookrecommend.book_recommend_be.service.book;

import com.bookrecommend.book_recommend_be.dto.request.BookRequest;
import com.bookrecommend.book_recommend_be.dto.response.BookResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IBookService {
    Page<BookResponse> getBooks(int page, int size);

    Page<BookResponse> getNewestBooks(int page, int size);

    Page<BookResponse> getMostReadBooks(int page, int size);

    Page<BookResponse> getBooksByGenre(Long genreId, int page, int size, String sort);

    Page<BookResponse> getAdminBooks(int page, int size, String keyword, Long genreId, String sort);

    BookResponse createBook(BookRequest request);

    BookResponse updateBook(Long id, BookRequest request);

    BookResponse getBookById(Long id);

    void deleteBook(Long id);

    int deleteBooks(List<Long> ids);

    Page<BookResponse> searchBooks(String keyword, int page, int size);

    String getBookFormatUrl(Long bookId, Long formatId);

    BookFileDownload getBookFileForDownload(Long bookId, Long formatId);
}
