package com.bookrecommend.book_recommend_be.service.user;

import com.bookrecommend.book_recommend_be.dto.request.UpdateUserRequest;
import com.bookrecommend.book_recommend_be.dto.response.UserResponse;
import com.bookrecommend.book_recommend_be.exceptions.ResourceNotFoundException;
import com.bookrecommend.book_recommend_be.model.User;
import com.bookrecommend.book_recommend_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = findUserOrThrow(userId);

        if (StringUtils.hasText(request.getUsername())) {
            user.setUsername(request.getUsername());
        }
        if (StringUtils.hasText(request.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (StringUtils.hasText(request.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse banUser(Long userId) {
        User user = findUserOrThrow(userId);
        user.setBan(true);
        User bannedUser = userRepository.save(user);
        return mapToResponse(bannedUser);
    }

    @Override
    @Transactional
    public UserResponse unbanUser(Long userId) {
        User user = findUserOrThrow(userId);
        user.setBan(false);
        User bannedUser = userRepository.save(user);
        return mapToResponse(bannedUser);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .activate(user.isActivate())
                .fullName(user.getFullName())
                .ban(user.isBan())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .build();
    }
}
