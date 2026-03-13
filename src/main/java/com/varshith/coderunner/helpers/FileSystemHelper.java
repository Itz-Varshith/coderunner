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

    @Value("${spring.testcases.base_path}")
    private String basePath;

    public int countTestCases(Path tempDir) {
        Path inputDir = tempDir.resolve("testcases").resolve("input");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDir)) {
            int count = 0;
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    count++;
                }
            }
            return count;
        } catch (IOException e) {
            return 0;
        }
    }

    public boolean createQuestionDirectory(String questionId) {
        Path path1= Paths.get(basePath,questionId);
        System.out.println(path1);
        try{
            Files.createDirectories(path1);
        } catch (IOException e) {
            System.out.println("Unable to create directory");
            return false;
        }
        return true;
    }

    public ValidatorResult extractZipToTemporary(MultipartFile zipFile, String questionId) {

        try {
            Path tempDir = Files.createTempDirectory("question_" + questionId);

            try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {

                ZipEntry entry;

                while ((entry = zis.getNextEntry()) != null) {

                    Path filePath = tempDir.resolve(entry.getName()).normalize();

                    // Zip Slip protection
                    if (!filePath.startsWith(tempDir)) {
                        return new ValidatorResult(false, null);
                    }

                    if (entry.isDirectory()) {
                        Files.createDirectories(filePath);
                        continue;
                    }

                    Files.createDirectories(filePath.getParent());

                    Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            return new ValidatorResult(true, tempDir);

        } catch (IOException e) {
            return new ValidatorResult(false, null);
        }
    }

    public boolean moveTempToQuestionDirectory(Path tempDir, String questionId) {

        try {

            Path finalDir = Paths.get(basePath, questionId);

            Files.move(tempDir, finalDir, StandardCopyOption.REPLACE_EXISTING);

            return true;

        } catch (IOException e) {
            return false;
        }
    }
}
