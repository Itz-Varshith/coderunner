package com.varshith.coderunner.helpers;


import com.varshith.coderunner.dtos.QuestionCreateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
/**
 * Validator for question requests checks for all the required fields in Question model.
 * */
@Component
public class QuestionValidator {
//    Avoiding too much size using config variable can be adjusted if the average sizes increase.
    @Value("${spring.testcases.max_size}")
    private double maxTestCaseSize;

//    Main validator function takes input QuestionCreateRequest.java and returns Boolean and String, String contains message post verification.
    public ValidatorResult<Boolean, String> validateQuestionData(QuestionCreateRequest question) {
        ValidatorResult<Boolean, String> result= new ValidatorResult<>(false, "Unable to process");

//        Check for null question data
        if (question == null){
            result.setSecondVariable("Question data cannot be null");
            return result;
        }

//        Check for empty title or non-existent title in request
        if (question.getTitle() == null || question.getTitle().isBlank()) {
            result.setSecondVariable("Question Title cannot be empty");
            return result;
        }

//        Check for question Markdown.
        if (question.getMarkdown() == null || question.getMarkdown().isBlank()) {
            result.setSecondVariable("Question Markdown cannot be empty");
            return result;
        }

//        Check for memory limit, must be positive.
        if (question.getMemory_limit() <= 0) {
            result.setSecondVariable("Question Memory Limit cannot be zero");
            return result;
        }

//        Check for time limit must be positive.
        if (question.getTime_limit() <= 0) {
            result.setSecondVariable("Question Time Limit cannot be zero");
            return result;
        }

//        Check for testcases zip file.
        if (question.getTest_cases() == null || question.getTest_cases().isEmpty()) {
            result.setSecondVariable("Question Test Cases cannot be empty");
            return result;
        }

//        Check for size of testcase zip file.
        double sizeMB = question.getTest_cases().getSize() / (1024.0 * 1024.0);

        if (sizeMB > maxTestCaseSize) {
            result.setSecondVariable("Question Test Cases size cannot be greater than maximum");
            return result;
        }

        if (question.getDifficulty() == null || question.getDifficulty().isBlank()) {
            result.setSecondVariable("Question Difficulty cannot be empty");
            return result;
        }

//        Check for files inside the testcases.zip for correct file format inside the zip.
        if(!validateQuestionDataBytes((question.getTest_cases()))){
            result.setSecondVariable("Question Test Cases Format invalid");
            return result;
        }
        result.setFirstVariable(true);
        result.setSecondVariable("Question Validation successful");
        return result;


    }

//    Main validator for data inside the files checks for Filenames existence fo judge file and folder structure.
    public boolean validateQuestionDataBytes(MultipartFile file) {

        int judgeFound = 0;
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                System.out.println("File inside zip: " + name);

                // judge.cpp
                if (name.equals("testcases/judge.cpp")) {
                    judgeFound++;
                    continue;
                }

                // input testcases
                if (name.startsWith("testcases/input/") && name.endsWith(".txt")) {
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
