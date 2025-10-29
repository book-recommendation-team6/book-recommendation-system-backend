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
        return bookRepository.findByIdAndIsDeletedFalse(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    private FavoriteResponse mapToFavoriteResponse(Favorite favorite) {
        Book book = favorite.getBook();

        return FavoriteResponse.builder()
                .id(favorite.getId())
                .userId(favorite.getUser().getId())
                .bookId(book.getId())
                .addedAt(favorite.getAddedAt())
                .book(mapToBookInfo(book))
                .build();
    }

    private FavoriteResponse.BookInfo mapToBookInfo(Book book) {
        return FavoriteResponse.BookInfo.builder()
                .id(book.getId())
                .title(book.getTitle())
                .description(book.getDescription())
                .coverImageUrl(book.getCoverImageUrl())
                .publicationYear(book.getPublicationYear())
                .publisher(book.getPublisher())
                .authors(book.getAuthors().stream()
                        .map(author -> new FavoriteResponse.AuthorInfo(author.getId(), author.getName()))
                        .collect(java.util.stream.Collectors.toSet()))
                .genres(book.getGenres().stream()
                        .map(genre -> new FavoriteResponse.GenreInfo(genre.getId(), genre.getName()))
                        .collect(java.util.stream.Collectors.toSet()))
                .formats(book.getFormats().stream()
                        .map(format -> new FavoriteResponse.FormatInfo(
                                format.getId(),
                                format.getType().getName(),
                                format.getTotalPages(),
                                format.getFileSizeKb(),
                                format.getContentUrl()))
                        .collect(java.util.stream.Collectors.toList()))
                .build();
    }
}
