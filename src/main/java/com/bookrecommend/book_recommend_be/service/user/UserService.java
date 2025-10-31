package com.bookrecommend.book_recommend_be.service.user;

import com.bookrecommend.book_recommend_be.dto.request.ChangePasswordRequest;
import com.bookrecommend.book_recommend_be.dto.request.UpdateUserRequest;
import com.bookrecommend.book_recommend_be.dto.response.UserResponse;
import com.bookrecommend.book_recommend_be.exceptions.ResourceNotFoundException;
import com.bookrecommend.book_recommend_be.model.User;
import com.bookrecommend.book_recommend_be.model.enums.UserStatus;
import com.bookrecommend.book_recommend_be.repository.UserRepository;
import com.bookrecommend.book_recommend_be.service.file.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;

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
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(int page, int size, String keyword, String status, String sortOption) {
        Sort sort = resolveSort(sortOption);
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<User> specification = buildSpecification(keyword, status);
        return userRepository.findAll(specification, pageable)
                .map(this::mapToResponse);
    }

    private Specification<User> buildSpecification(String keyword, String status) {
        Specification<User> spec = nonAdminSpecification();

        if (StringUtils.hasText(keyword)) {
            spec = spec.and(keywordSpecification(keyword.trim()));
        }

        Specification<User> statusSpec = statusSpecification(status);
        if (statusSpec != null) {
            spec = spec.and(statusSpec);
        }

        return spec;
    }

    private Specification<User> nonAdminSpecification() {
        return (root, query, cb) -> cb.notEqual(cb.upper(root.join("role").get("name")), "ADMIN");
    }

    private Specification<User> keywordSpecification(String keyword) {
        String trimmed = keyword.trim();
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        String likePattern = "%" + normalized + "%";

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("username")), likePattern));
            predicates.add(cb.like(cb.lower(root.get("email")), likePattern));
            predicates.add(cb.like(cb.lower(root.get("fullName")), likePattern));
            predicates.add(cb.like(cb.lower(root.get("phoneNumber")), likePattern));

            if (isNumeric(trimmed)) {
                try {
                    Long id = Long.parseLong(trimmed);
                    predicates.add(cb.equal(root.get("id"), id));
                } catch (NumberFormatException ex) {
                    log.debug("Failed to parse keyword '{}' as user id", trimmed, ex);
                }
            }

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<User> statusSpecification(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }

        try {
            UserStatus resolvedStatus = UserStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
            return switch (resolvedStatus) {
                case BANNED -> (root, query, cb) -> cb.isTrue(root.get("ban"));
                case ACTIVE -> (root, query, cb) -> cb.and(
                        cb.isFalse(root.get("ban")),
                        cb.isTrue(root.get("activate"))
                );
                case INACTIVE -> (root, query, cb) -> cb.and(
                        cb.isFalse(root.get("ban")),
                        cb.isFalse(root.get("activate"))
                );
            };
        } catch (IllegalArgumentException ex) {
            log.warn("Unknown status filter provided: {}", status);
            return null;
        }
    }

    private Sort resolveSort(String sortOption) {
        String normalized = StringUtils.hasText(sortOption)
                ? sortOption.trim().toLowerCase(Locale.ROOT)
                : "newest";

        return switch (normalized) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "name-asc" -> Sort.by(Sort.Direction.ASC, "username").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "name-desc" -> Sort.by(Sort.Direction.DESC, "username").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "email-asc" -> Sort.by(Sort.Direction.ASC, "email").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "email-desc" -> Sort.by(Sort.Direction.DESC, "email").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> {
                log.warn("Unknown sort option provided: {}. Falling back to newest first.", sortOption);
                yield Sort.by(Sort.Direction.DESC, "createdAt");
            }
        };
    }

    private boolean isNumeric(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional
    public UserResponse banUser(Long userId) {
        List<UserResponse> responses = banUsers(java.util.Collections.singletonList(userId));
        if (responses.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return responses.get(0);
    }

    @Override
    @Transactional
    public List<UserResponse> banUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("At least one user id is required");
        }

        java.util.LinkedHashSet<Long> normalizedSet = new java.util.LinkedHashSet<>();
        for (Long id : userIds) {
            if (id != null) {
                normalizedSet.add(id);
            }
        }

        if (normalizedSet.isEmpty()) {
            throw new IllegalArgumentException("At least one valid user id is required");
        }

        List<Long> normalizedIds = new ArrayList<>(normalizedSet);
        List<User> users = userRepository.findAllById(normalizedIds);

        java.util.Map<Long, User> usersById = new java.util.LinkedHashMap<>();
        for (User user : users) {
            usersById.putIfAbsent(user.getId(), user);
        }

        List<Long> missingIds = new ArrayList<>();
        for (Long id : normalizedIds) {
            if (!usersById.containsKey(id)) {
                missingIds.add(id);
            }
        }

        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException("Users not found with ids: " + missingIds);
        }

        List<Long> adminIds = new ArrayList<>();
        usersById.values().forEach(user -> {
            if (user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName())) {
                adminIds.add(user.getId());
            }
        });

        if (!adminIds.isEmpty()) {
            throw new IllegalArgumentException("Cannot ban administrator accounts: " + adminIds);
        }

        usersById.values().forEach(user -> user.setBan(true));
        userRepository.saveAll(usersById.values());

        List<UserResponse> responses = new ArrayList<>();
        for (Long id : normalizedIds) {
            responses.add(mapToResponse(usersById.get(id)));
        }
        return responses;
    }

    @Override
    @Transactional
    public UserResponse unbanUser(Long userId) {
        User user = findUserOrThrow(userId);
        user.setBan(false);
        User bannedUser = userRepository.save(user);
        return mapToResponse(bannedUser);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUserOrThrow(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
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
                .fullName(user.getFullName())
                .status(UserStatus.fromUser(user))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .build();
    }
}
