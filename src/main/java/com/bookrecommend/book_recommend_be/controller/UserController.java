package com.bookrecommend.book_recommend_be.controller;

import com.bookrecommend.book_recommend_be.dto.request.UpdateUserRequest;
import com.bookrecommend.book_recommend_be.dto.response.ApiResponse;
import com.bookrecommend.book_recommend_be.dto.response.UserResponse;
import com.bookrecommend.book_recommend_be.service.user.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id,
                                                                @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "User information updated successfully"));
    }

    @PatchMapping("/{id}/ban")
    public ResponseEntity<ApiResponse<UserResponse>> banUser(@PathVariable Long id) {
        UserResponse response = userService.banUser(id);
        return ResponseEntity.ok(ApiResponse.success(response, "User banned successfully"));
    }

    @PatchMapping("/{id}/unban")
    public ResponseEntity<ApiResponse<UserResponse>> unbanUser(@PathVariable Long id) {
        UserResponse response = userService.unbanUser(id);
        return ResponseEntity.ok(ApiResponse.success(response, "User banned successfully"));
    }
}