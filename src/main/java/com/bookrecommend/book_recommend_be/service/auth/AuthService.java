package com.bookrecommend.book_recommend_be.service.auth;

import com.bookrecommend.book_recommend_be.dto.request.LoginRequest;
import com.bookrecommend.book_recommend_be.dto.request.RegisterRequest;
import com.bookrecommend.book_recommend_be.dto.request.ResetPasswordRequest;
import com.bookrecommend.book_recommend_be.dto.response.LoginResponse;
import com.bookrecommend.book_recommend_be.model.Role;
import com.bookrecommend.book_recommend_be.model.User;
import com.bookrecommend.book_recommend_be.repository.RoleRepository;
import com.bookrecommend.book_recommend_be.repository.UserRepository;
import com.bookrecommend.book_recommend_be.security.jwt.JwtUtils;
import com.bookrecommend.book_recommend_be.security.userdetails.AppUserDetails;
import com.bookrecommend.book_recommend_be.service.email.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final IEmailService emailService;

    @Value("${app.auth.verification-url}")
    private String verificationUrl;

    @Value("${app.auth.reset-password-url}")
    private String resetPasswordUrl;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateAccessToken(authentication);

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        return new LoginResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse("")
        );
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(userRole);
        user.setActivate(false);
        user.setCreatedAt(Instant.now());

        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(Instant.now().plus(24, ChronoUnit.HOURS));

        userRepository.save(user);

        String verificationLink = buildUrl(verificationUrl, verificationToken);
        emailService.sendEmail(
                user.getEmail(),
                "Verify your email",
                "Please verify your email by clicking the following link: " + verificationLink
        );
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (user.getEmailVerificationTokenExpiry() == null || user.getEmailVerificationTokenExpiry().isBefore(Instant.now())) {
            user.setEmailVerificationToken(null);
            user.setEmailVerificationTokenExpiry(null);
            userRepository.save(user);
            throw new RuntimeException("Verification token has expired");
        }

        user.setActivate(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new RuntimeException("User not found with the provided email");
        }

        user.setResetPasswordToken(UUID.randomUUID().toString());
        user.setResetPasswordTokenExpiry(Instant.now().plus(1, ChronoUnit.HOURS));
        userRepository.save(user);

        String resetLink = buildUrl(resetPasswordUrl, user.getResetPasswordToken());
        emailService.sendEmail(
                user.getEmail(),
                "Reset your password",
                "You can reset your password by clicking the following link: " + resetLink
        );
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetPasswordToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid reset password token"));

        if (user.getResetPasswordTokenExpiry() == null || user.getResetPasswordTokenExpiry().isBefore(Instant.now())) {
            user.setResetPasswordToken(null);
            user.setResetPasswordTokenExpiry(null);
            userRepository.save(user);
            throw new RuntimeException("Reset password token has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);
    }

    private String buildUrl(String baseUrl, String token) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new RuntimeException("Base URL for email action is not configured");
        }

        if (baseUrl.contains("?")) {
            return baseUrl + token;
        }

        if (baseUrl.endsWith("/")) {
            return baseUrl + token;
        }

        return baseUrl + "?token=" + token;
    }
}