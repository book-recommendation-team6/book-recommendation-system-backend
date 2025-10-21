package com.bookrecommend.book_recommend_be.minio;

import java.time.LocalDate;
import java.util.UUID;

public class KeyUtil {
    public static String forFilename(String filename) {
        String ext = "pdf";
        int dot = filename.lastIndexOf('.');
        if (dot >= 0 && dot < filename.length()-1) {
            ext = filename.substring(dot+1).toLowerCase();
        }
        String today = LocalDate.now().toString(); // YYYY-MM-DD
        return "uploads/%s/%s.%s".formatted(today, UUID.randomUUID(), ext);
    }
}