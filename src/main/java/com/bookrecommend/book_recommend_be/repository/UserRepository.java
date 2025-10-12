package com.bookrecommend.book_recommend_be.repository;

import com.bookrecommend.book_recommend_be.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    boolean existsByEmail(@NotBlank @Email @Size(max = 100) String email);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByResetPasswordToken(String token);
}
