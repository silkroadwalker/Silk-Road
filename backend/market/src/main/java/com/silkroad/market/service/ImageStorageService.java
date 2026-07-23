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

/**
 * Service class responsible for handling image file storage and retrieval.
 * 
 * <p>
 * This service provides file system operations for storing, loading, and
 * deleting image files associated with advertisements. Images are stored in a
 * local directory with unique filenames to prevent collisions.
 * </p>
 * 
 * <p>
 * Supported image formats:
 * <ul>
 * <li>JPEG</li>
 * <li>PNG</li>
 * <li>WebP</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Known Limitation:</b> There is currently no size limit enforcement
 * for uploaded images, though this is noted as a planned enhancement.
 * </p>
 * 
 * @author Silkroad Market Team
 * @version 1.0
 * @see MultipartFile
 */
@Service
public class ImageStorageService {

    private static final String UPLOAD_DIRECTORY = "uploads";

    /**
     * Retrieves the upload directory path and creates it if it doesn't exist.
     * 
     * @return the normalized absolute path to the upload directory
     * @throws IOException if the directory cannot be created
     */
    private Path getUploadPath() throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIRECTORY).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        return uploadPath;
    }

    /**
     * Saves an image file to the local filesystem.
     * 
     * <p>
     * This method validates that the file is not empty and that the content
     * type is one of the supported image formats. The file is saved with a
     * unique UUID filename to prevent naming conflicts.
     * </p>
     * 
     * <p>
     * <b>Todo:</b> Add file size validation to limit image size.
     * </p>
     * 
     * @param file the image file to save
     * @return the generated unique filename
     * @throws IOException if the file is empty, not an image, or cannot be saved
     */
    public String saveImage(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IOException("Image is empty.");
        }

        String contentType = file.getContentType();

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

    /**
     * Deletes an image file from the local filesystem.
     * 
     * @param fileName the name of the file to delete
     * @throws IOException if the file cannot be deleted
     */
    public void deleteImage(String fileName) throws IOException {

        Path path = getUploadPath().resolve(fileName);

        Files.deleteIfExists(path);
    }

    /**
     * Loads an image file as a Spring Resource.
     * 
     * @param fileName the name of the file to load
     * @return a Resource object for the image file
     * @throws IOException if the file is not found or cannot be read
     */
    public Resource loadImage(String fileName) throws IOException {

        Path path = Paths.get(UPLOAD_DIRECTORY).resolve(fileName);

        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("Image not found.");
        }

        return resource;
    }
}