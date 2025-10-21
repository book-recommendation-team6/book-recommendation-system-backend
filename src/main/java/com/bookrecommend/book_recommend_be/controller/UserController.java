package com.bookrecommend.book_recommend_be.controller;

import com.bookrecommend.book_recommend_be.dto.request.UpdateUserRequest;
import com.bookrecommend.book_recommend_be.dto.response.ApiResponse;
import com.bookrecommend.book_recommend_be.dto.response.UserResponse;
import com.bookrecommend.book_recommend_be.model.User;
import com.bookrecommend.book_recommend_be.repository.UserRepository;
import com.bookrecommend.book_recommend_be.security.userdetails.AppUserDetails;
import com.bookrecommend.book_recommend_be.service.user.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final UserRepository userRepository;

    @PutMapping("/{id}/update")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id,
                                                                @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "User information updated successfully"));
    }

    @PatchMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> updateUserAvatar(@PathVariable Long id,
                                                                      @RequestPart("avatar") MultipartFile avatarFile) {
        UserResponse response = userService.updateUserAvatar(id, avatarFile);
        return ResponseEntity.ok(ApiResponse.success(response, "User avatar updated successfully"));
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

    @GetMapping("/profile")
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
}
