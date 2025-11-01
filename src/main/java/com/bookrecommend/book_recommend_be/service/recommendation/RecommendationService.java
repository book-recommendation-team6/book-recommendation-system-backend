package com.bookrecommend.book_recommend_be.service.recommendation;

import com.bookrecommend.book_recommend_be.dto.recommendation.RecommendationItem;
import com.bookrecommend.book_recommend_be.dto.recommendation.RecommendationsResponse;
import com.bookrecommend.book_recommend_be.dto.recommendation.SimilarBooksResponse;
import com.bookrecommend.book_recommend_be.dto.response.BookResponse;
import com.bookrecommend.book_recommend_be.service.book.IBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RestTemplate restTemplate;
    private final IBookService bookService;

    @Value("${recsys.url}")
    private String recsysUrl;

    public List<BookResponse> getRecommendations(Long userId, int limit) {
        try {
            String url = String.format("%s/recommendations?user_id=%d&limit=%d",
                    recsysUrl, userId, limit);

            RecommendationsResponse response = restTemplate.getForObject(
                    url, RecommendationsResponse.class);

            if (response == null || response.getItems() == null) {
                return Collections.emptyList();
            }

            // Map recommendation items to full book responses
            return response.getItems().stream()
                    .map(item -> {
                        try {
                            return bookService.getBookById(item.getBookId());
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(book -> book != null)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<BookResponse> getSimilarBooks(Long bookId, int limit) {
        try {
            String url = String.format("%s/similar?book_id=%d&limit=%d",
                    recsysUrl, bookId, limit);

            SimilarBooksResponse response = restTemplate.getForObject(
                    url, SimilarBooksResponse.class);

            if (response == null || response.getItems() == null) {
                return Collections.emptyList();
            }

            // Map similar items to full book responses
            return response.getItems().stream()
                    .map(item -> {
                        try {
                            return bookService.getBookById(item.getBookId());
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(book -> book != null)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

}