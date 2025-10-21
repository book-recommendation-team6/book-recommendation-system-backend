package com.bookrecommend.book_recommend_be.minio;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("${api.prefix}")
@RequiredArgsConstructor
public class UploadController {

    private final MinioClient minio;

    @Value("${minio.bucket}")
    private String bucket;

    // Tạo key an toàn: /uploads/YYYYMM/<uuid>.pdf
    private String makeObjectKey(@NotNull String originalName) {
        String ext = "pdf";
        String name = StringUtils.cleanPath(originalName);
        int i = name.lastIndexOf('.');
        if (i >= 0 && i < name.length() - 1) {
            ext = name.substring(i + 1).toLowerCase();
        }
        String yyyymm = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMM"));
        return "uploads/%s/%s.%s".formatted(yyyymm, UUID.randomUUID(), ext);
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> uploadDirect(@RequestPart("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "file is empty"));
        }

        // Chỉ nhận PDF
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains("pdf")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only PDF is allowed"));
        }

        // Tạo object key
        String key = makeObjectKey(file.getOriginalFilename() == null ? "file.pdf" : file.getOriginalFilename());

        // Upload thẳng vào MinIO.
        // Ở đây ta biết kích thước (file.getSize()), nên truyền thẳng để MinIO không cần multipart ngầm.
        minio.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(key)
                        .contentType("application/pdf")
                        .stream(file.getInputStream(), file.getSize(), -1) // objectSize, partSize=-1 vì đã có size
                        .build()
        );

        // (Tuỳ chọn) set thêm user metadata
        // .userMetadata(Map.of("x-amz-meta-origin-name", new String(file.getOriginalFilename().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8)))

        return ResponseEntity.ok(Map.of(
                "key", key,
                "bucket", bucket,
                "contentType", "application/pdf",
                "size", file.getSize()
        ));
    }
}