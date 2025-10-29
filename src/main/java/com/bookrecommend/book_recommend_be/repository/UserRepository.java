package com.bookrecommend.book_recommend_be.repository;

import com.bookrecommend.book_recommend_be.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    boolean existsByEmail(@NotBlank @Email @Size(max = 100) String email);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByResetPasswordToken(String token);

    @Query("""
                SELECT CAST(u.createdAt AS date) AS registrationDate,
                       COUNT(u) AS registrations
                FROM User u
                WHERE u.createdAt >= :startDate
                GROUP BY CAST(u.createdAt AS date)
                ORDER BY CAST(u.createdAt AS date)
            """)
    List<Object[]> countNewUsersByDate(@Param("startDate") Instant startDate);

    boolean existsByUsername(@NotBlank @Size(max = 50) String username);

    boolean existsByPhoneNumber(@NotBlank @Size(max = 50) String username);

    @Query("""
            SELECT u FROM User u
            WHERE UPPER(u.role.name) <> 'ADMIN'
            ORDER BY u.createdAt DESC
            """)
    Page<User> findNonAdminUsers(Pageable pageable);

    @Query("""
            SELECT u FROM User u
            WHERE UPPER(u.role.name) <> 'ADMIN'
              AND (
                    LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR CONCAT(u.id, '') LIKE CONCAT('%', :keyword, '%')
              )
            ORDER BY u.createdAt DESC
            """)
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
}
