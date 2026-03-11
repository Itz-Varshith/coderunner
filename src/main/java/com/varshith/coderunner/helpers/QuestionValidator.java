package com.varshith.coderunner.helpers;


import com.varshith.coderunner.dtos.QuestionCreateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
public class QuestionValidator {
    @Value("${spring.testcases.max_size}")
    private double maxTestCaseSize;
    public boolean validateQuestionData(QuestionCreateRequest question) {
        if (question == null) return false;

        if (question.getTitle() == null || question.getTitle().isBlank()) return false;

        if (question.getMarkdown() == null || question.getMarkdown().isBlank()) return false;

        if (question.getMemory_limit() <= 0) return false;

        if (question.getTime_limit() <= 0) return false;

        if (question.getTest_cases() == null || question.getTest_cases().isEmpty()) return false;

        double sizeMB = question.getTest_cases().getSize() / (1024.0 * 1024.0);

        if (sizeMB > maxTestCaseSize) return false;

        if (!Set.of("EASY","MEDIUM","HARD").contains(question.getDifficulty())) return false;

        return validateQuestionDataBytes(question.getTest_cases());


    }

    public boolean validateQuestionDataBytes(MultipartFile testCases) {
        // Here we need to unzip files and also check for extensions and correct structure of data.
        return false;
    }

}
