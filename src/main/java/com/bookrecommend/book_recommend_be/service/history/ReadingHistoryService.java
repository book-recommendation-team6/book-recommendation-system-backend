package com.bookrecommend.book_recommend_be.service.history;

import com.bookrecommend.book_recommend_be.dto.request.ReadingHistoryRequest;
import com.bookrecommend.book_recommend_be.dto.response.ReadingHistoryResponse;
import com.bookrecommend.book_recommend_be.exceptions.ResourceNotFoundException;
import com.bookrecommend.book_recommend_be.model.Book;
import com.bookrecommend.book_recommend_be.model.ReadingHistory;
import com.bookrecommend.book_recommend_be.model.User;
import com.bookrecommend.book_recommend_be.repository.BookRepository;
import com.bookrecommend.book_recommend_be.repository.ReadingHistoryRepository;
import com.bookrecommend.book_recommend_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

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
                .map(existing -> {
                    existing.setProgress(request.getProgress());
                    existing.setLastReadAt(Instant.now());
                    return existing;
                })
                .orElseGet(() -> ReadingHistory.builder()
                        .user(user)
                        .book(book)
                        .progress(request.getProgress())
                        .lastReadAt(Instant.now())
                        .build());

        ReadingHistory savedHistory = readingHistoryRepository.save(history);
        return mapToReadingHistoryResponse(savedHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadingHistoryResponse> getUserReadingHistory(Long userId) {
        ensureUserExists(userId);
        return readingHistoryRepository.findAllByUserIdOrderByLastReadAtDesc(userId).stream()
                .map(this::mapToReadingHistoryResponse)
                .toList();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private Book getBookOrThrow(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    private ReadingHistoryResponse mapToReadingHistoryResponse(ReadingHistory history) {
        return ReadingHistoryResponse.builder()
                .id(history.getId())
                .userId(history.getUser().getId())
                .bookId(history.getBook().getId())
                .progress(history.getProgress())
                .lastReadAt(history.getLastReadAt())
                .build();
    }
}
