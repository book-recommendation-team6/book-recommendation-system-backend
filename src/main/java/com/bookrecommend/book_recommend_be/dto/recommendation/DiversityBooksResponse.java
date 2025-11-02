package com.bookrecommend.book_recommend_be.dto.recommendation;

import com.bookrecommend.book_recommend_be.dto.response.BookResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiversityBooksResponse {

    private List<BookResponse> items;
}
