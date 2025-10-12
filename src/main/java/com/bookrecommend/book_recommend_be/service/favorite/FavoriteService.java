package com.bookrecommend.book_recommend_be.service.favorite;

import com.bookrecommend.book_recommend_be.dto.response.FavoriteResponse;
import com.bookrecommend.book_recommend_be.exceptions.ResourceNotFoundException;
import com.bookrecommend.book_recommend_be.model.Book;
import com.bookrecommend.book_recommend_be.model.Favorite;
import com.bookrecommend.book_recommend_be.model.User;
import com.bookrecommend.book_recommend_be.repository.BookRepository;
import com.bookrecommend.book_recommend_be.repository.FavoriteRepository;
import com.bookrecommend.book_recommend_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService implements IFavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public FavoriteResponse addFavorite(Long userId, Long bookId) {
        User user = getUserOrThrow(userId);
        Book book = getBookOrThrow(bookId);

        Favorite favorite = favoriteRepository.findByUserIdAndBookId(userId, bookId)
                .orElseGet(() -> favoriteRepository.save(Favorite.builder()
                        .user(user)
                        .book(book)
                        .build()));

        return mapToFavoriteResponse(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long bookId) {
        Favorite favorite = favoriteRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite not found for user and book"));
        favoriteRepository.delete(favorite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteResponse> getUserFavorites(Long userId) {
        ensureUserExists(userId);
        return favoriteRepository.findAllByUserIdOrderByAddedAtDesc(userId).stream()
                .map(this::mapToFavoriteResponse)
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

    private FavoriteResponse mapToFavoriteResponse(Favorite favorite) {
        return FavoriteResponse.builder()
                .id(favorite.getId())
                .userId(favorite.getUser().getId())
                .bookId(favorite.getBook().getId())
                .addedAt(favorite.getAddedAt())
                .build();
    }
}
