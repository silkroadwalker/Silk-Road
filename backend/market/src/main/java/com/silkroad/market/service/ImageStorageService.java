package com.silkroad.market.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorageService {

    private static final String UPLOAD_DIRECTORY = "uploads";

    private Path getUploadPath() throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIRECTORY).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        return uploadPath;
    }

    public String saveImage(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IOException("Image is empty.");
        }

        String contentType = file.getContentType();

        // todo: limit the image entry by size
        if (contentType == null ||
                !(contentType.equals("image/jpeg")
                        || contentType.equals("image/png")
                        || contentType.equals("image/webp"))) {

            throw new IOException("Only image files are allowed.");
        }

        Path uploadPath = getUploadPath();

        String originalFilename = file.getOriginalFilename();

        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID() + extension;

        Path destination = uploadPath.resolve(fileName);

        Files.copy(
                file.getInputStream(),
                destination,
                StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    public void deleteImage(String fileName) throws IOException {

        Path path = getUploadPath().resolve(fileName);

        Files.deleteIfExists(path);
    }

    public Resource loadImage(String fileName) throws IOException {

        Path path = Paths.get(UPLOAD_DIRECTORY).resolve(fileName);

        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("Image not found.");
        }

        return resource;
    }
}