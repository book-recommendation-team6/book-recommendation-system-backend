package com.bookrecommend.book_recommend_be.service.genre;

import com.bookrecommend.book_recommend_be.dto.response.GenreResponse;
import com.bookrecommend.book_recommend_be.model.Genre;
import com.bookrecommend.book_recommend_be.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenreService implements IGenreService {

    private final GenreRepository genreRepository;

    @Override
    public Page<GenreResponse> getGenres(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return genreRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    private GenreResponse mapToResponse(Genre genre) {
        return new GenreResponse(
                genre.getId(),
                genre.getName(),
                genre.getDescription()
        );
    }
}
