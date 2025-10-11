package com.bookrecommend.book_recommend_be.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "authors", schema = "book_recommendation_system")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_id")
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "author_name", nullable = false, length = 100)
    private String name;

    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;

    @ManyToMany(mappedBy = "authors")
    private Set<Book> books = new HashSet<>();
}
