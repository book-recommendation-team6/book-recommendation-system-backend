package com.bookrecommend.book_recommend_be.service.author;

import com.bookrecommend.book_recommend_be.dto.response.AuthorResponse;
import com.bookrecommend.book_recommend_be.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class IAuthorService implements AuthorService{

    private final AuthorRepository authorRepository;

    @Override
    public List<AuthorResponse> getAllAuthors() {
        return authorRepository.findAll().stream()
                .map(author -> AuthorResponse.builder()
                        .id(author.getId())
                        .name(author.getName())
                        .biography(author.getBiography())
                        .build())
                .collect(Collectors.toList());
    }
}
