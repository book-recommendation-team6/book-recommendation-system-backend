package com.bookrecommend.book_recommend_be.service.history;

import com.bookrecommend.book_recommend_be.dto.request.ReadingHistoryRequest;
import com.bookrecommend.book_recommend_be.dto.response.ReadingHistoryResponse;
import com.bookrecommend.book_recommend_be.exceptions.ResourceNotFoundException;
import com.bookrecommend.book_recommend_be.model.Author;
import com.bookrecommend.book_recommend_be.model.Book;
import com.bookrecommend.book_recommend_be.model.ReadingHistory;
import com.bookrecommend.book_recommend_be.model.User;
import com.bookrecommend.book_recommend_be.repository.BookRepository;
import com.bookrecommend.book_recommend_be.repository.ReadingHistoryRepository;
import com.bookrecommend.book_recommend_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadingHistoryService implements IReadingHistoryService {

    private final ReadingHistoryRepository readingHistoryRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public ReadingHistoryResponse recordReadingHistory(Long userId, Long bookId, ReadingHistoryRequest request) {
        User user = getUserOrThrow(userId);
        Book book = getBookOrThrow(bookId);

        ReadingHistory history = readingHistoryRepository.findByUserIdAndBookId(userId, bookId)
                .orElseGet(() -> ReadingHistory.builder()
                        .user(user)
                        .book(book)
                        .build());

        history.setProgress(request.getProgress());
        history.setLastReadAt(Instant.now());

        ReadingHistory savedHistory = readingHistoryRepository.save(history);
        return mapToReadingHistoryResponse(savedHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReadingHistoryResponse> getUserReadingHistory(Long userId, int page, int size) {
        ensureUserExists(userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastReadAt"));
        return readingHistoryRepository.findAllByUserId(userId, pageable)
                .map(this::mapToReadingHistoryResponse);
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

    private ReadingHistoryResponse mapToReadingHistoryResponse(ReadingHistory history) {
        Book book = history.getBook();
        List<ReadingHistoryResponse.AuthorSummary> authors = book.getAuthors().stream()
                .map(this::mapToAuthorSummary)
                .collect(Collectors.toList());

        return ReadingHistoryResponse.builder()
                .id(history.getId())
                .userId(history.getUser().getId())
                .bookId(book.getId())
                .progress(history.getProgress())
                .lastReadAt(history.getLastReadAt())
                .book(ReadingHistoryResponse.BookSummary.builder()
                        .id(book.getId())
                        .title(book.getTitle())
                        .coverImageUrl(book.getCoverImageUrl())
                        .authors(authors)
                        .build())
                .build();
    }

    private ReadingHistoryResponse.AuthorSummary mapToAuthorSummary(Author author) {
        return ReadingHistoryResponse.AuthorSummary.builder()
                .id(author.getId())
                .name(author.getName())
                .build();
    }
}
