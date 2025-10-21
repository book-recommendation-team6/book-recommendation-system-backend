package com.bookrecommend.book_recommend_be.service.genre;

import com.bookrecommend.book_recommend_be.dto.response.GenreResponse;
import com.bookrecommend.book_recommend_be.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IGenreService implements GenreService {

    private final GenreRepository genreRepository;

    @Override
    public List<GenreResponse> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(genre -> GenreResponse.builder()
                        .id(genre.getId())
                        .name(genre.getName())
                        .description(genre.getDescription())
                        .build())
                .collect(Collectors.toList());
    }
}