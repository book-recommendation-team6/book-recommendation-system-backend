package com.bookrecommend.book_recommend_be.controller;

import com.bookrecommend.book_recommend_be.dto.response.AdminDashboardResponse;
import com.bookrecommend.book_recommend_be.dto.response.ApiResponse;
import com.bookrecommend.book_recommend_be.service.dashboard.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final IDashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard(
            @RequestParam(defaultValue = "0") int topRatedPage,
            @RequestParam(defaultValue = "5") int topRatedSize,
            @RequestParam(defaultValue = "0") int topFavoritedPage,
            @RequestParam(defaultValue = "5") int topFavoritedSize) {
        AdminDashboardResponse response = dashboardService.getDashboardData(
                topRatedPage,
                topRatedSize,
                topFavoritedPage,
                topFavoritedSize
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Admin dashboard data retrieved successfully"));
    }
}
