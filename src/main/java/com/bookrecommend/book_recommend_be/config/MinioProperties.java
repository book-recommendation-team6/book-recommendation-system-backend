package com.bookrecommend.book_recommend_be.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    /**
     * MinIO server endpoint, e.g. http://localhost:9000.
     */
    private String endpoint;

    /**
     * Access key for MinIO authentication.
     */
    private String accessKey;

    /**
     * Secret key for MinIO authentication.
     */
    private String secretKey;

    /**
     * Bucket where book contents are stored.
     */
    private String bucketName;

    /**
     * Optional external/public endpoint for constructing direct URLs.
     */
    private String publicEndpoint;

    /**
     * Expiration (seconds) for presigned URLs.
     */
    private long presignedExpirySeconds = 3600;
}
