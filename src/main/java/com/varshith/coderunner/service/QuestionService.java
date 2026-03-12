package com.varshith.coderunner.service;


import com.varshith.coderunner.dtos.APIResponse;
import com.varshith.coderunner.dtos.QuestionCreateRequest;
import com.varshith.coderunner.helpers.FileSystemHelper;
import com.varshith.coderunner.helpers.Pair;
import com.varshith.coderunner.helpers.QuestionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;


// I recently learnt that required args constructor directly converts service injections into constructor based ones.
@Service
@RequiredArgsConstructor
public class QuestionService {



    private QuestionValidator questionValidator;
    private FileSystemHelper fileSystemHelper;

    public APIResponse<String> createQuestion(QuestionCreateRequest questionCreateRequest)  {
    /*
    * Steps
    * 1) Validate Request
    * 2) Create question ID
    * 3) Create system directory
    * 4) Decode Zip file
    * 5) Save in appropriate places
    * 6) Create question entry in table
    * 7) return
    * */
        APIResponse<String> response = new APIResponse<>();
        response.setDate(new Date());
        response.setSuccess(false);

        Pair<Boolean, String> questionValidationResult=questionValidator.validateQuestionData(questionCreateRequest);
        if(!questionValidationResult.first()){
            response.setData(questionValidationResult.second());
            response.setMessage("Invalid data provided");
            return  response;
        }

        String questionId=UUID.randomUUID().toString();
        Boolean questionDirectoryCreation=fileSystemHelper.createQuestionDirectory(questionId);
        if(!questionDirectoryCreation){
            response.setMessage("Unable to create directory");
            response.setData("Unable to create directory for question");
            return response;
        }

        Pair<Boolean, String> extractionResult= fileSystemHelper.extractZipToTemporary(questionCreateRequest.getTest_cases(), questionId);
        if(!extractionResult.first()){
            response.setMessage("Extraction failed");
            response.setData(extractionResult.second());
            return response;
        }


        // Continue flow.
        return response;
    }
}
