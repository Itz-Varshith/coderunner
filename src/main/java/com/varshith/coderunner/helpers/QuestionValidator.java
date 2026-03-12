package com.varshith.coderunner.helpers;


import com.varshith.coderunner.dtos.QuestionCreateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class QuestionValidator {
    @Value("${spring.testcases.max_size}")
    private double maxTestCaseSize;
    public Pair<Boolean, String> validateQuestionData(QuestionCreateRequest question) {
        Pair<Boolean, String> result= new Pair<Boolean, String>(false, "Unable to process");
        if (question == null){
            result.setSecondVariable("Question data cannot be null");
            return result;
        }

        if (question.getTitle() == null || question.getTitle().isBlank()) {
            result.setSecondVariable("Question Title cannot be empty");
            return result;
        }

        if (question.getMarkdown() == null || question.getMarkdown().isBlank()) {
            result.setSecondVariable("Question Markdown cannot be empty");
            return result;
        }

        if (question.getMemory_limit() <= 0) {
            result.setSecondVariable("Question Memory Limit cannot be zero");
            return result;
        }

        if (question.getTime_limit() <= 0) {
            result.setSecondVariable("Question Time Limit cannot be zero");
            return result;
        }

        if (question.getTest_cases() == null || question.getTest_cases().isEmpty()) {
            result.setSecondVariable("Question Test Cases cannot be empty");
            return result;
        }

        double sizeMB = question.getTest_cases().getSize() / (1024.0 * 1024.0);

        if (sizeMB > maxTestCaseSize) {
            result.setSecondVariable("Question Test Cases size cannot be greater than maximum");
            return result;
        }
        System.out.println(question.getDifficulty());
        if (question.getDifficulty() == null || question.getDifficulty().isBlank()) {
            result.setSecondVariable("Question Difficulty cannot be empty");
            return result;
        }

        if(validateQuestionDataBytes((question.getTest_cases()))){
            result.setSecondVariable("Question Test Cases Format invalid");
            return result;
        }
        result.setFirstVariable(true);
        result.setSecondVariable("Question Validation successful");
        return result;


    }

    public boolean validateQuestionDataBytes(MultipartFile file) {

        int judgeFound = 0;
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) return false;
                String name = entry.getName();
                System.out.println("File inside zip: " + name);

                // judge.cpp
                if (name.equals("judge.cpp")) {
                    judgeFound++;
                    continue;
                }

                // input testcases
                if (name.startsWith("input/") && name.endsWith(".txt")) {
                    continue;
                }

                return false;
            }

        } catch (IOException err) {
            return false;
        }

        return (judgeFound==1);
    }

}
