package com.varshith.coderunner.helpers;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FileSystemHelper {

    @Value("$spring.testcases.base_path")
    private String basePath;

    public boolean createQuestionDirectory(String questionId) {
        Path path1= Paths.get(basePath,questionId);
        Path path2= Paths.get(basePath,questionId);
        try{
            Files.createDirectories(path1);
            Files.createDirectories(path2);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public Pair<Boolean, String> extractZipToTemporary(MultipartFile zipFile, String questionId) {

        try {
            Path tempDir = Files.createTempDirectory("question_" + questionId);
            try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path filePath = tempDir.resolve(entry.getName()).normalize();
                    // Zip Slip protection
                    if (!filePath.startsWith(tempDir)) {
                        return new Pair<>(false, "Invalid zip entry detected");
                    }
                    if (entry.isDirectory()) {
                        Files.createDirectories(filePath);
                        continue;
                    }
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

        } catch (IOException e) {
            return new Pair<>(false, "Error extracting zip file");
        }
        return new Pair<>(true, "question_" + questionId);
    }
}
