package com.bookrecommend.book_recommend_be.service.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface IFileStorageService {
    String storeFile(MultipartFile file, String bookTitle, String formatType);
    
    void deleteFile(String fileUrl);
    
    boolean isValidBookFile(MultipartFile file);
    
    Integer getFileSizeInKb(MultipartFile file);
    
    Resource loadFileAsResource(String fileUrl);
}