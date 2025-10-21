package com.bookrecommend.book_recommend_be.service.book;

import com.bookrecommend.book_recommend_be.dto.request.BookRequest;
import com.bookrecommend.book_recommend_be.dto.response.BookResponse;
import com.bookrecommend.book_recommend_be.dto.response.ImageUploadResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IBookService {
    Page<BookResponse> getBooks(int page, int size);

    Page<BookResponse> getNewestBooks(int page, int size);

    Page<BookResponse> getMostReadBooks(int page, int size);

    Page<BookResponse> getBooksByGenre(Long genreId, int page, int size);

    BookResponse getBookById(Long id);

    BookResponse createBook(BookRequest request);

    ImageUploadResponse uploadCoverImage(MultipartFile coverFile);

    BookResponse updateCoverImage(Long bookId, MultipartFile coverFile);
    
    BookResponse createBookWithFiles(
        String title,
        String description,
        String coverImageUrl,
        Integer publicationYear,
        String publisher,
        List<String> authorNames,
        List<Long> genreIds,
        MultipartFile file
    );

    BookResponse updateBook(Long id, BookRequest request);
    
    BookResponse updateBookWithFiles(
        Long id,
        String title,
        String description,
        String coverImageUrl,
        Integer publicationYear,
        String publisher,
        List<String> authorNames,
        List<Long> genreIds,
        MultipartFile file
    );

    void deleteBook(Long id);

    Page<BookResponse> searchBooks(String keyword, int page, int size);
    
    String getBookFormatUrl(Long bookId, Long formatId);

}
