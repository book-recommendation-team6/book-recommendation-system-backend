package com.bookrecommend.book_recommend_be.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @NotBlank
    @Size(max = 50)
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Size(max = 15)
    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Size(max = 255)
    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "is_activate", nullable = false)
    private boolean activate = false;

    @Size(max = 100)
    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "is_ban", nullable = false)
    private boolean ban = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
