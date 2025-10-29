package com.bookrecommend.book_recommend_be.service.bookmark;

import com.bookrecommend.book_recommend_be.dto.request.BookmarkRequest;
import com.bookrecommend.book_recommend_be.dto.response.BookmarkResponse;
import com.bookrecommend.book_recommend_be.exceptions.ResourceNotFoundException;
import com.bookrecommend.book_recommend_be.model.Book;
import com.bookrecommend.book_recommend_be.model.Bookmark;
import com.bookrecommend.book_recommend_be.model.User;
import com.bookrecommend.book_recommend_be.repository.BookRepository;
import com.bookrecommend.book_recommend_be.repository.BookmarkRepository;
import com.bookrecommend.book_recommend_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkService implements IBookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public BookmarkResponse createBookmark(Long userId, Long bookId, BookmarkRequest request) {
        User user = getUserOrThrow(userId);
        Book book = getBookOrThrow(bookId);

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .book(book)
                .pageNumber(request.getPageNumber())
                .locationInBook(request.getLocationInBook())
                .build();

        Bookmark saved = bookmarkRepository.save(bookmark);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteBookmark(Long userId, Long bookmarkId) {
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found with id: " + bookmarkId));

        if (!bookmark.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Bookmark does not belong to user: " + userId);
        }

        bookmarkRepository.delete(bookmark);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkResponse> getBookmarksForBook(Long userId, Long bookId) {
        ensureUserExists(userId);
        ensureBookExists(bookId);
        return bookmarkRepository.findAllByUserIdAndBookIdOrderByCreatedAtDesc(userId, bookId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private Book getBookOrThrow(Long bookId) {
        return bookRepository.findByIdAndIsDeletedFalse(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    private void ensureBookExists(Long bookId) {
        if (!bookRepository.existsByIdAndIsDeletedFalse(bookId)) {
            throw new ResourceNotFoundException("Book not found with id: " + bookId);
        }
    }

    private BookmarkResponse mapToResponse(Bookmark bookmark) {
        return BookmarkResponse.builder()
                .id(bookmark.getId())
                .userId(bookmark.getUser().getId())
                .bookId(bookmark.getBook().getId())
                .pageNumber(bookmark.getPageNumber())
                .locationInBook(bookmark.getLocationInBook())
                .createdAt(bookmark.getCreatedAt())
                .build();
    }
}
