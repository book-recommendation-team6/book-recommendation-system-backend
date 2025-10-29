package com.bookrecommend.book_recommend_be.service.history;

import com.bookrecommend.book_recommend_be.dto.request.ReadingHistoryRequest;
import com.bookrecommend.book_recommend_be.dto.response.ReadingHistoryResponse;
import org.springframework.data.domain.Page;

public interface IReadingHistoryService {

    ReadingHistoryResponse recordReadingHistory(Long userId, Long bookId, ReadingHistoryRequest request);

    Page<ReadingHistoryResponse> getUserReadingHistory(Long userId, int page, int size);
}
