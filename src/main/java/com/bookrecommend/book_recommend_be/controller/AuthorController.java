package com.bookrecommend.book_recommend_be.controller;

import com.bookrecommend.book_recommend_be.dto.response.ApiResponse;
import com.bookrecommend.book_recommend_be.dto.response.AuthorResponse;
import com.bookrecommend.book_recommend_be.service.author.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuthorResponse>>> getAllAuthors() {
        List<AuthorResponse> authors = authorService.getAllAuthors();
        return ResponseEntity.ok(ApiResponse.<List<AuthorResponse>>builder()
                .message("Get all authors successfully")
                .data(authors)
                .build());
    }
}