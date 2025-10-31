package com.bookrecommend.book_recommend_be.service.user;

import com.bookrecommend.book_recommend_be.dto.request.ChangePasswordRequest;
import com.bookrecommend.book_recommend_be.dto.request.UpdateUserRequest;
import com.bookrecommend.book_recommend_be.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUserService {
    Page<UserResponse> getUsers(int page, int size, String keyword, String status, String sort);

    UserResponse updateUser(Long userId, UpdateUserRequest request);

    UserResponse banUser(Long userId);

    UserResponse unbanUser(Long userId);

    UserResponse updateUserAvatar(Long userId, MultipartFile avatarFile);

    void changePassword(Long userId, ChangePasswordRequest request);

    List<UserResponse> banUsers(List<Long> userIds);
}
