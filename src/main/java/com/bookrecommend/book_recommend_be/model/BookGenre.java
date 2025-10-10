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
@Table(name = "book_genres")
public class BookGenre {

    @EmbeddedId
    private BookGenreId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookId")
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("genreId")
    @JoinColumn(name = "genre_id")
    private Genre genre;

    @Getter
    @Setter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    public static class BookGenreId implements Serializable {

        @Column(name = "book_id")
        private Long bookId;

        @Column(name = "genre_id")
        private Long genreId;
    }
}