package com.bookrecommend.book_recommend_be.service.file;

import org.springframework.web.multipart.MultipartFile;

public interface IFileStorageService {
    StoredFile storeFile(MultipartFile file, String bookTitle, String formatType);

    void deleteFile(String objectKey);

    boolean isValidBookFile(MultipartFile file);

    int calculateFileSizeKb(long sizeBytes);

    String generatePresignedUrl(String objectKey);
}
