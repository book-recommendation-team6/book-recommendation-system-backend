package com.bookrecommend.book_recommend_be.service.file;

import com.bookrecommend.book_recommend_be.config.MinioProperties;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService implements IFileStorageService {

    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    @Value("${file.max-size-mb}")
    private long maxSizeMb;
    private long maxSizeBytes;

    @PostConstruct
    public void init() {
        this.maxSizeBytes = maxSizeMb * 1024 * 1024;
        ensureBucket();
    }

    @Override
    public StoredFile storeFile(MultipartFile file, String bookTitle, String formatType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (!isValidBookFile(file)) {
            throw new IllegalArgumentException("Invalid file type. Only PDF and EPUB files are allowed");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " + maxSizeMb + "MB");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename);
        String objectName = buildObjectName(bookTitle, formatType, extension);
        String contentType = StringUtils.hasText(file.getContentType())
                ? file.getContentType()
                : "application/octet-stream";

        try (InputStream inputStream = file.getInputStream()) {
            putObject(objectName, inputStream, file.getSize(), contentType);

            log.info("Stored file in MinIO: {}", objectName);
            return StoredFile.builder()
                    .objectKey(objectName)
                    .fileName(originalFilename)
                    .sizeBytes(file.getSize())
                    .contentType(contentType)
                    .totalPages(null)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read input file", e);
        }
    }

    @Override
    public void deleteFile(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .build());
            log.info("Deleted file from MinIO: {}", objectKey);
        } catch (Exception e) {
            log.warn("Failed to delete file '{}' from MinIO: {}", objectKey, e.getMessage());
        }
    }

    @Override
    public boolean isValidBookFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            return false;
        }
        String extension = getFileExtension(originalFilename).toLowerCase(Locale.ROOT);
        if (!"pdf".equals(extension) && !"epub".equals(extension)) {
            return false;
        }
        String contentType = file.getContentType();
        if ("pdf".equals(extension)) {
            return contentType == null
                    || contentType.isBlank()
                    || "application/pdf".equalsIgnoreCase(contentType)
                    || "application/octet-stream".equalsIgnoreCase(contentType);
        }
        return contentType == null
                || contentType.isBlank()
                || "application/epub+zip".equalsIgnoreCase(contentType)
                || "application/octet-stream".equalsIgnoreCase(contentType);
    }

    @Override
    public int calculateFileSizeKb(long sizeBytes) {
        if (sizeBytes <= 0) {
            return 0;
        }
        return (int) Math.max(1, Math.ceil(sizeBytes / 1024.0));
    }

    @Override
    public String generatePresignedUrl(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            throw new IllegalArgumentException("Object key must not be empty");
        }
        if (StringUtils.hasText(minioProperties.getPublicEndpoint())) {
            return minioProperties.getPublicEndpoint().replaceAll("/+$", "") + "/"
                    + minioProperties.getBucketName() + "/" + objectKey;
        }
        try {
            long configuredExpiry = minioProperties.getPresignedExpirySeconds();
            int expirySeconds = (int) Math.max(60, Math.min(604800, configuredExpiry));
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .method(Method.GET)
                    .expiry(expirySeconds)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    @Override
    public DownloadedFile getFile(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            throw new IllegalArgumentException("Object key must not be empty");
        }

        try {
            StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .build());

            GetObjectResponse response = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectKey)
                    .build());

            long size = stat.size();
            String contentType = stat.contentType();

            return DownloadedFile.builder()
                    .inputStream(response)
                    .contentType(contentType)
                    .sizeBytes(size)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from storage", e);
        }
    }

    private void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .build());
                log.info("Created MinIO bucket '{}'", minioProperties.getBucketName());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify MinIO bucket", e);
        }
    }

    private void putObject(String objectName, InputStream inputStream, long size, String contentType) {
        try {
            long partSize = Math.max(5 * 1024 * 1024, size);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .stream(inputStream, size, partSize)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            if (e instanceof MinioException minioException) {
                log.error("MinIO error storing object {}: {}", objectName, minioException.getMessage());
            }
            throw new RuntimeException("Failed to store file to MinIO", e);
        }
    }

    private String buildObjectName(String bookTitle, String formatType, String extension) {
        String sanitizedTitle = sanitizeTitle(bookTitle);
        String datePath = DATE_PATH_FORMATTER.format(LocalDate.now());
        String safeExtension = extension.isBlank() ? "bin" : extension;
        return String.format("books/%s/%s_%s_%s.%s",
                datePath,
                sanitizedTitle,
                formatType.toLowerCase(Locale.ROOT),
                UUID.randomUUID().toString().substring(0, 8),
                safeExtension);
    }

    private String sanitizeTitle(String title) {
        if (!StringUtils.hasText(title)) {
            return "book";
        }
        String normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String sanitized = normalized
                .replaceAll("[^a-zA-Z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "_")
                .toLowerCase(Locale.ROOT);
        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 50);
        }
        return sanitized.isBlank() ? "book" : sanitized;
    }

    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

}
