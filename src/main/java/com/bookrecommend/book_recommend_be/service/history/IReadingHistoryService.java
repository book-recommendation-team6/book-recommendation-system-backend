package com.bookrecommend.book_recommend_be.service.history;

import com.bookrecommend.book_recommend_be.dto.request.ReadingHistoryRequest;
import com.bookrecommend.book_recommend_be.dto.response.ReadingHistoryResponse;

import java.util.List;

public interface IReadingHistoryService {

    ReadingHistoryResponse recordReadingHistory(Long userId, Long bookId, ReadingHistoryRequest request);

    List<ReadingHistoryResponse> getUserReadingHistory(Long userId);
}
