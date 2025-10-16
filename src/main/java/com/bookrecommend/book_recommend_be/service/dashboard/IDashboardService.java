package com.bookrecommend.book_recommend_be.service.dashboard;

import com.bookrecommend.book_recommend_be.dto.response.AdminDashboardResponse;

public interface IDashboardService {
    AdminDashboardResponse getDashboardData(int topRatedPage, int topRatedSize,
                                            int topFavoritedPage, int topFavoritedSize);
}