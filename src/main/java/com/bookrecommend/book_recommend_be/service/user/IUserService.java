package com.bookrecommend.book_recommend_be.service.user;

import com.bookrecommend.book_recommend_be.dto.request.UpdateUserRequest;
import com.bookrecommend.book_recommend_be.dto.response.UserResponse;

public interface IUserService {
    UserResponse updateUser(Long userId, UpdateUserRequest request);

    UserResponse banUser(Long userId);

    UserResponse unbanUser(Long userId);
}
