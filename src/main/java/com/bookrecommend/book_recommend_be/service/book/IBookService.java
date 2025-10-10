package com.bookrecommend.book_recommend_be.service.book;

import com.bookrecommend.book_recommend_be.dto.request.BookRequest;
import com.bookrecommend.book_recommend_be.dto.response.BookResponse;
import org.springframework.data.domain.Page;

public interface IBookService {
    Page<BookResponse> getBooks(int page, int size);

    BookResponse getBookById(Long id);

    BookResponse createBook(BookRequest request);

    BookResponse updateBook(Long id, BookRequest request);

    void deleteBook(Long id);
}
