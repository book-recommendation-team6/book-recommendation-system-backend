package com.bookrecommend.book_recommend_be.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopFavoritedBookResponse {
    private Long id;
    private String title;
    private String coverImageUrl;
    private long favoriteCount;
}
