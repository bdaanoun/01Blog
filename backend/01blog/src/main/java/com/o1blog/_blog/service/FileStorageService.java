package com.o1blog._blog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private final String TEMP_DIR = "temp";
    private final String PERMANENT_DIR = "posts";

    public String saveTemp(MultipartFile file) {
        try {
            String filename = UUID.randomUUID().toString() + "_" +
                    file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.\\-]", "_");

            Path tempPath = Paths.get(uploadDir, TEMP_DIR);
            Files.createDirectories(tempPath);

            Path filePath = tempPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Temp file saved to: " + filePath);
            return filename;

        } catch (IOException e) {
            System.err.println("Failed to save temp file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to store temporary file", e);
        }
    }

    public String save(MultipartFile file) {
        try {
            String filename = UUID.randomUUID().toString() + "_" +
                    file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.\\-]", "_");

            Path permanentPath = Paths.get(uploadDir, PERMANENT_DIR);
            Files.createDirectories(permanentPath);

            Path filePath = permanentPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return PERMANENT_DIR + "/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public String moveTempToPermanent(String tempFilename) {
        try {
            System.out.println("Moving temp file: " + tempFilename);

            Path tempPath = Paths.get(uploadDir, TEMP_DIR, tempFilename);

            if (!Files.exists(tempPath)) {
                System.err.println("Temp file not found: " + tempPath);
                // If file doesn't exist in temp, it might already be permanent
                // Just return the filename
                return PERMANENT_DIR + "/" + tempFilename;
            }

            Path permanentDir = Paths.get(uploadDir, PERMANENT_DIR);
            Files.createDirectories(permanentDir);

            Path newPath = permanentDir.resolve(tempFilename);
            Files.move(tempPath, newPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("File moved to: " + newPath);
            return PERMANENT_DIR + "/" + tempFilename;

        } catch (IOException e) {
            System.err.println("Error moving file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to move file", e);
        }
    }
}