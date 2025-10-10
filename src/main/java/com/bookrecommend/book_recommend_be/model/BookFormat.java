package com.bookrecommend.book_recommend_be.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "book_formats")
public class BookFormat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "format_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id", nullable = false)
    private BookType type;

    @NotBlank
    @Size(max = 255)
    @Column(name = "content_url", nullable = false, length = 255)
    private String contentUrl;

    @Column(name = "total_pages")
    private Integer totalPages;

    @Column(name = "file_size_kb")
    private Integer fileSizeKb;
}
