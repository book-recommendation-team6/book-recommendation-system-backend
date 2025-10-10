package com.bookrecommend.book_recommend_be.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "book_authors")
public class BookAuthor {
    @EmbeddedId
    private BookAuthorId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookId")
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("authorId")
    @JoinColumn(name = "author_id")
    private Author author;

    @Getter
    @Setter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    public static class BookAuthorId implements Serializable {
        @Column(name = "book_id")
        private Long bookId;

        @Column(name = "author_id")
        private Long authorId;
    }
}
