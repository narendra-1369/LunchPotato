package com.lunchpotato.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FoodImageStorageService {

    private final Path baseDirectory = Path.of("uploads").toAbsolutePath().normalize();

    public FoodImageStorageService() throws IOException {
        Files.createDirectories(baseDirectory.resolve("food"));
        Files.createDirectories(baseDirectory.resolve("restaurants"));
    }

    // Existing food image uploads continue using this method.
    public String saveImage(MultipartFile file) throws IOException {
        return saveImage(file, "food");
    }

    // Use category "restaurants" when saving a restaurant logo.
    public String saveImage(MultipartFile file, String category)
            throws IOException {

        if (!category.equals("food") && !category.equals("restaurants")) {
            throw new IllegalArgumentException("Unsupported image category");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select an image.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "Image must be smaller than 5 MB.");
        }

        String contentType = file.getContentType();
        String extension;

        if ("image/jpeg".equalsIgnoreCase(contentType)) {
            extension = ".jpg";
        } else if ("image/png".equalsIgnoreCase(contentType)) {
            extension = ".png";
        } else if ("image/gif".equalsIgnoreCase(contentType)) {
            extension = ".gif";
        } else {
            throw new IllegalArgumentException(
                    "Only JPG, PNG and GIF images are allowed.");
        }

        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new IllegalArgumentException("Invalid image file.");
        }

        String filename = UUID.randomUUID() + extension;
        Path destination = baseDirectory.resolve(category).resolve(filename);

        Files.copy(file.getInputStream(), destination);

        return "/uploads/" + category + "/" + filename;
    }
}