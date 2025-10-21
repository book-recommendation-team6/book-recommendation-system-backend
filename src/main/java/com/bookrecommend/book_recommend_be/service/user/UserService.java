package com.bookrecommend.book_recommend_be.service.user;

import com.bookrecommend.book_recommend_be.dto.request.UpdateUserRequest;
import com.bookrecommend.book_recommend_be.dto.response.UserResponse;
import com.bookrecommend.book_recommend_be.exceptions.ResourceNotFoundException;
import com.bookrecommend.book_recommend_be.model.User;
import com.bookrecommend.book_recommend_be.repository.UserRepository;
import com.bookrecommend.book_recommend_be.service.file.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

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
    public UserResponse updateUserAvatar(Long userId, MultipartFile avatarFile) {
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new IllegalArgumentException("Avatar file must not be empty");
        }

        User user = findUserOrThrow(userId);
        String previousAvatarUrl = user.getAvatarUrl();

        var uploadResponse = cloudinaryService.uploadImage(avatarFile, "book_recommend/users");
        String resolvedUrl = StringUtils.hasText(uploadResponse.getSecureUrl())
                ? uploadResponse.getSecureUrl()
                : uploadResponse.getUrl();

        user.setAvatarUrl(resolvedUrl);
        User savedUser = userRepository.save(user);

        if (StringUtils.hasText(previousAvatarUrl) && !previousAvatarUrl.equals(resolvedUrl)) {
            cloudinaryService.extractPublicIdFromUrl(previousAvatarUrl)
                    .ifPresent(publicId -> {
                        try {
                            cloudinaryService.deleteImage(publicId);
                        } catch (Exception ex) {
                            log.warn("Failed to delete old avatar from Cloudinary: {}", publicId, ex);
                        }
                    });
        }

        return mapToResponse(savedUser);
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
