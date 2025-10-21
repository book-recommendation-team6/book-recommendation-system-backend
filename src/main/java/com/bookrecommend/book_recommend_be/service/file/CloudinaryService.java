package com.bookrecommend.book_recommend_be.service.file;

import com.bookrecommend.book_recommend_be.dto.response.ImageUploadResponse;
import com.bookrecommend.book_recommend_be.exceptions.ImageProcessingException;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private static final String DEFAULT_FOLDER = "book_recommend";

    private final Cloudinary cloudinary;

    public ImageUploadResponse uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new ImageProcessingException("Image file must not be empty");
        }

        String targetFolder = StringUtils.hasText(folder) ? folder : DEFAULT_FOLDER;

        Map<String, Object> params = new HashMap<>();
        params.put("folder", targetFolder);
        params.put("resource_type", "image");
        params.put("unique_filename", true);
        params.put("overwrite", true);

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            return ImageUploadResponse.builder()
                    .url((String) uploadResult.get("url"))
                    .secureUrl((String) uploadResult.get("secure_url"))
                    .publicId((String) uploadResult.get("public_id"))
                    .build();
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new ImageProcessingException("Failed to upload image to Cloudinary", e);
        }
    }

    public void deleteImage(String publicId) {
        if (!StringUtils.hasText(publicId)) {
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, Map.of());
        } catch (IOException e) {
            log.warn("Failed to delete image from Cloudinary: {}", publicId, e);
        }
    }

    public Optional<String> extractPublicIdFromUrl(String imageUrl) {
        if (!StringUtils.hasText(imageUrl) || !imageUrl.contains("/upload/")) {
            return Optional.empty();
        }

        try {
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                return Optional.empty();
            }

            String afterUpload = parts[1];

            if (afterUpload.contains("?")) {
                afterUpload = afterUpload.substring(0, afterUpload.indexOf("?"));
            }

            int lastDotIndex = afterUpload.lastIndexOf('.');
            if (lastDotIndex > 0) {
                afterUpload = afterUpload.substring(0, lastDotIndex);
            }

            if (afterUpload.startsWith("/")) {
                afterUpload = afterUpload.substring(1);
            }

            String[] segments = afterUpload.split("/");
            if (segments.length > 0 && segments[0].startsWith("v") && segments[0].substring(1).matches("\\d+")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
            }

            return StringUtils.hasText(afterUpload) ? Optional.of(afterUpload) : Optional.empty();
        } catch (Exception e) {
            log.warn("Unable to extract Cloudinary publicId from url: {}", imageUrl, e);
            return Optional.empty();
        }
    }
}
