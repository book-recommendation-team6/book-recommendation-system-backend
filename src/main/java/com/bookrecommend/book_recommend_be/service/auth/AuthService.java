package com.bookrecommend.book_recommend_be.service.auth;

import com.bookrecommend.book_recommend_be.dto.request.LoginRequest;
import com.bookrecommend.book_recommend_be.dto.request.RegisterRequest;
import com.bookrecommend.book_recommend_be.dto.response.LoginResponse;
import com.bookrecommend.book_recommend_be.model.Role;
import com.bookrecommend.book_recommend_be.model.User;
import com.bookrecommend.book_recommend_be.repository.RoleRepository;
import com.bookrecommend.book_recommend_be.repository.UserRepository;
import com.bookrecommend.book_recommend_be.security.jwt.JwtUtils;
import com.bookrecommend.book_recommend_be.security.userdetails.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
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
        user.setCreatedAt(Instant.now());

        userRepository.save(user);
    }
}
