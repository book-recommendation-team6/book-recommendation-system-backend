package com.bookrecommend.book_recommend_be.controller;

import com.bookrecommend.book_recommend_be.dto.request.BulkIdsRequest;
import com.bookrecommend.book_recommend_be.dto.request.ChangePasswordRequest;
import com.bookrecommend.book_recommend_be.dto.request.UpdateUserRequest;
import com.bookrecommend.book_recommend_be.dto.response.ApiResponse;
import com.bookrecommend.book_recommend_be.dto.response.UserResponse;
import com.bookrecommend.book_recommend_be.model.User;
import com.bookrecommend.book_recommend_be.repository.UserRepository;
import com.bookrecommend.book_recommend_be.security.userdetails.AppUserDetails;
import com.bookrecommend.book_recommend_be.service.user.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final UserRepository userRepository;

    @PutMapping("/users/{id}/update")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id,
                                                                @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "User information updated successfully"));
    }

    @PatchMapping(value = "/users/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> updateUserAvatar(@PathVariable Long id,
                                                                      @RequestPart("avatar") MultipartFile avatarFile) {
        UserResponse response = userService.updateUserAvatar(id, avatarFile);
        return ResponseEntity.ok(ApiResponse.success(response, "User avatar updated successfully"));
    }

    @PatchMapping("/users/{id}/ban")
    public ResponseEntity<ApiResponse<UserResponse>> banUser(@PathVariable Long id) {
        UserResponse response = userService.banUser(id);
        return ResponseEntity.ok(ApiResponse.success(response, "User banned successfully"));
    }

    @PatchMapping("/users/ban")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> banUsers(
            @Valid @RequestBody BulkIdsRequest request) {
        int bannedCount = userService.banUsers(request.getIds()).size();
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("bannedCount", bannedCount),
                "Users banned successfully"));
    }

    @PatchMapping("/users/{id}/unban")
    public ResponseEntity<ApiResponse<UserResponse>> unbanUser(@PathVariable Long id) {
        UserResponse response = userService.unbanUser(id);
        return ResponseEntity.ok(ApiResponse.success(response, "User unbanned successfully"));
    }

    @PatchMapping("/users/{id}/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@PathVariable Long id,
                                                            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    @GetMapping("/users/profile")
    public User getProfile(@AuthenticationPrincipal AppUserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("User not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getEmail());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }

    @GetMapping("/admin/users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, name = "sort") String sortOption) {
        Page<UserResponse> users = userService.getUsers(page, size, keyword, status, sortOption);
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }
}
