package com.bookrecommend.book_recommend_be.service.user;

import com.bookrecommend.book_recommend_be.dto.request.UpdateUserRequest;
import com.bookrecommend.book_recommend_be.dto.response.UserResponse;
import org.springframework.data.domain.Page;

public interface IUserService {
    Page<UserResponse> getAllUsers(int page, int size);

    UserResponse updateUser(Long userId, UpdateUserRequest request);

    UserResponse banUser(Long userId);

    UserResponse unbanUser(Long userId);
}
