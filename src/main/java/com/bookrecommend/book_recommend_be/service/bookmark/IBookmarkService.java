package com.bookrecommend.book_recommend_be.service.bookmark;

import com.bookrecommend.book_recommend_be.dto.request.BookmarkRequest;
import com.bookrecommend.book_recommend_be.dto.response.BookmarkResponse;

import java.util.List;

public interface IBookmarkService {

    BookmarkResponse createBookmark(Long userId, Long bookId, BookmarkRequest request);

    void deleteBookmark(Long userId, Long bookmarkId);

    List<BookmarkResponse> getBookmarksForBook(Long userId, Long bookId);
}