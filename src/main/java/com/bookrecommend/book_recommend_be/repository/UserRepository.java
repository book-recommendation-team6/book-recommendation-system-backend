package com.bookrecommend.book_recommend_be.repository;

import com.bookrecommend.book_recommend_be.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    boolean existsByEmail(@NotBlank(message = "Email must not be blank") @Email(message = "Invalid email format") String email);
}
