package com.bookrecommend.book_recommend_be.service.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService implements IFileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${file.allowed-extensions}")
    private String allowedExtensions;
    
    @Value("${file.max-size-mb}")
    private long maxSizeMb;
    
    private Path baseStorageLocation;
    private List<String> allowedExtensionsList;
    private long maxSizeBytes;
    
    @PostConstruct
    public void init() {
        // Parse allowed extensions
        this.allowedExtensionsList = Arrays.asList(allowedExtensions.split(","));
        this.maxSizeBytes = maxSizeMb * 1024 * 1024;
        
        // Create base upload directory
        this.baseStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.baseStorageLocation);
            log.info("Created base file storage directory: {}", this.baseStorageLocation);
        } catch (Exception ex) {
            log.error("Could not create base upload directory", ex);
            throw new RuntimeException("Could not create base upload directory", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String bookTitle, String formatType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file
        if (!isValidBookFile(file)) {
            throw new IllegalArgumentException("Invalid file type. Only " + allowedExtensions + " files are allowed");
        }

        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " + maxSizeMb + "MB");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        
        // Clean book title for filename
        String sanitizedBookTitle = bookTitle
                .replaceAll("[^a-zA-Z0-9\\s-]", "")
                .replaceAll("\\s+", "_")
                .toLowerCase();
        sanitizedBookTitle = sanitizedBookTitle.substring(0, Math.min(sanitizedBookTitle.length(), 50));
        // Generate unique filename
        String fileName = String.format("%s_%s_%s.%s",
                sanitizedBookTitle,
                formatType.toLowerCase(),
                UUID.randomUUID().toString().substring(0, 8),
                fileExtension);

        // Generate date folder dynamically
        String dateFolder = new SimpleDateFormat("yyyy/MM").format(new Date());
        Path fileStorageLocation = baseStorageLocation.resolve(dateFolder);

        try {
            // Security check
            if (fileName.contains("..")) {
                throw new RuntimeException("Invalid file path");
            }

            // Create date subfolder if not exists
            Files.createDirectories(fileStorageLocation);
            
            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("File stored successfully: {}", targetLocation);
            
            // Return relative path for URL
            return "/uploads/books/" + dateFolder + "/" + fileName;
            
        } catch (IOException ex) {
            log.error("Failed to store file: {}", fileName, ex);
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Extract relative path from URL
            String relativePath = fileUrl.replace("/uploads/books/", "");
            Path filePath = Paths.get(uploadDir).resolve(relativePath).normalize();
            if (!filePath.startsWith(baseStorageLocation)) {
                throw new SecurityException("Access denied");
            }
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted file: {}", filePath);
            }
        } catch (IOException ex) {
            log.error("Failed to delete file: {}", fileUrl, ex);
            // Don't throw exception for delete failures
        }
    }

    @Override
    public boolean isValidBookFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }

        // Check extension
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!allowedExtensionsList.contains(extension)) {
            return false;
        }
        
        // Validate actual MIME type from file content
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        // Map of allowed MIME types
        boolean isValidMimeType = switch (extension) {
            case "pdf" -> contentType.equals("application/pdf");
            case "docx" -> contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case "doc" -> contentType.equals("application/msword");
            default -> false;
        };
        
        return isValidMimeType;
    }
    
    @Override
    public Integer getFileSizeInKb(MultipartFile file) {
        if (file == null) {
            return null;
        }
        return (int) (file.getSize() / 1024);
    }

    @Override
    public Resource loadFileAsResource(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("File URL is empty");
        }

        try {
            // Extract relative path from URL
            String relativePath = fileUrl.replace("/uploads/books/", "");
            Path filePath = baseStorageLocation.resolve(relativePath).normalize();
            if (!filePath.startsWith(baseStorageLocation)) {
                throw new SecurityException("Access denied");
            }
            
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + fileUrl);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Invalid file path: " + fileUrl, ex);
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}