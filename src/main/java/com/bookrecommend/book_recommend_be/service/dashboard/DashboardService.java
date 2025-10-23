package com.bookrecommend.book_recommend_be.service.dashboard;

import com.bookrecommend.book_recommend_be.dto.response.AdminDashboardResponse;
import com.bookrecommend.book_recommend_be.dto.response.dashboard.DailyUserRegistrationResponse;
import com.bookrecommend.book_recommend_be.dto.response.dashboard.TopFavoritedBookResponse;
import com.bookrecommend.book_recommend_be.dto.response.dashboard.TopRatedBookResponse;
import com.bookrecommend.book_recommend_be.repository.AuthorRepository;
import com.bookrecommend.book_recommend_be.repository.BookRepository;
import com.bookrecommend.book_recommend_be.repository.GenreRepository;
import com.bookrecommend.book_recommend_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final AuthorRepository authorRepository;

    @Override
    public AdminDashboardResponse getDashboardData(int topRatedPage, int topRatedSize,
                                                   int topFavoritedPage, int topFavoritedSize) {
        long totalUsers = userRepository.count();
        long totalBooks = bookRepository.count();
        long totalGenres = genreRepository.count();
        long totalAuthors = authorRepository.count();

        List<DailyUserRegistrationResponse> newUsersLast7Days = buildNewUsersLast7Days();

        Page<TopRatedBookResponse> topRatedBooks = bookRepository
                .findTopRatedBooks(PageRequest.of(topRatedPage, topRatedSize))
                .map(this::mapToTopRatedBookResponse);

        Page<TopFavoritedBookResponse> topFavoritedBooks = bookRepository
                .findTopFavoritedBooks(PageRequest.of(topFavoritedPage, topFavoritedSize))
                .map(this::mapToTopFavoritedBookResponse);

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalBooks(totalBooks)
                .totalGenres(totalGenres)
                .totalAuthors(totalAuthors)
                .newUsersLast7Days(newUsersLast7Days)
                .topRatedBooks(topRatedBooks)
                .topFavoritedBooks(topFavoritedBooks)
                .build();
    }

    private List<DailyUserRegistrationResponse> buildNewUsersLast7Days() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate startDate = today.minusDays(6);
        Instant startInstant = startDate.atStartOfDay(ZoneOffset.UTC).toInstant();

        List<Object[]> rawCounts = userRepository.countNewUsersByDate(startInstant);

        Map<LocalDate, Long> countsByDate = new HashMap<>();
        for (Object[] record : rawCounts) {
            if (record == null || record.length < 2) {
                continue;
            }
            LocalDate date = convertToLocalDate(record[0]);
            Long count = record[1] instanceof Number ? ((Number) record[1]).longValue() : null;
            if (date != null && count != null) {
                countsByDate.put(date, count);
            }
        }

        List<DailyUserRegistrationResponse> responses = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            long count = countsByDate.getOrDefault(date, 0L);
            responses.add(new DailyUserRegistrationResponse(date, count));
        }
        return responses;
    }

    private LocalDate convertToLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
        }
        if (value instanceof Instant instant) {
            return instant.atZone(ZoneOffset.UTC).toLocalDate();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toLocalDate();
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toLocalDate();
        }
        if (value instanceof ZonedDateTime zonedDateTime) {
            return zonedDateTime.toLocalDate();
        }
        return null;
    }

    private TopRatedBookResponse mapToTopRatedBookResponse(Object[] row) {
        if (row == null || row.length < 5) {
            return new TopRatedBookResponse(null, null, null, 0D, 0L);
        }
        Long bookId = row[0] != null ? ((Number) row[0]).longValue() : null;
        String title = (String) row[1];
        String coverImageUrl = (String) row[2];
        double averageRating = row[3] instanceof Number ? ((Number) row[3]).doubleValue() : 0D;
        long ratingCount = row[4] instanceof Number ? ((Number) row[4]).longValue() : 0L;
        return new TopRatedBookResponse(bookId, title, coverImageUrl, averageRating, ratingCount);
    }

    private TopFavoritedBookResponse mapToTopFavoritedBookResponse(Object[] row) {
        if (row == null || row.length < 4) {
            return new TopFavoritedBookResponse(null, null, null, 0L);
        }
        Long bookId = row[0] != null ? ((Number) row[0]).longValue() : null;
        String title = (String) row[1];
        String coverImageUrl = (String) row[2];
        long favoriteCount = row[3] instanceof Number ? ((Number) row[3]).longValue() : 0L;
        return new TopFavoritedBookResponse(bookId, title, coverImageUrl, favoriteCount);
    }
}