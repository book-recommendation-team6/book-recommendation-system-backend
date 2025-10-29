package com.bookrecommend.book_recommend_be.dto.response;

import com.bookrecommend.book_recommend_be.dto.response.dashboard.DailyUserRegistrationResponse;
import com.bookrecommend.book_recommend_be.dto.response.dashboard.TopFavoritedBookResponse;
import com.bookrecommend.book_recommend_be.dto.response.dashboard.TopRatedBookResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalBooks;
    private long totalGenres;
    private long totalAuthors;
    private List<DailyUserRegistrationResponse> newUsersLast7Days;
    private Page<TopRatedBookResponse> topRatedBooks;
    private Page<TopFavoritedBookResponse> topFavoritedBooks;
}
