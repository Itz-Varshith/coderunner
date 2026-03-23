package com.varshith.coderunner.service;


import com.varshith.coderunner.dtos.APIResponse;
import com.varshith.coderunner.dtos.QuestionCreateRequest;
import com.varshith.coderunner.dtos.QuestionFetchAllResponse;
import com.varshith.coderunner.dtos.QuestionFetchResponse;
import com.varshith.coderunner.helpers.FileSystemHelper;
import com.varshith.coderunner.helpers.ValidatorResult;
import com.varshith.coderunner.helpers.QuestionValidator;
import com.varshith.coderunner.models.QuestionModel;
import com.varshith.coderunner.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.UUID;


// I recently learnt that required args constructor directly converts service injections into constructor based ones.
@Service
@RequiredArgsConstructor
public class QuestionService {

    @Value("${spring.testcases.base_path}")
    private String basePath;


    private final QuestionValidator questionValidator;
    private final FileSystemHelper fileSystemHelper;
    private final QuestionRepository questionRepository;



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

        ValidatorResult<Boolean, String> questionValidationResult=questionValidator.validateQuestionData(questionCreateRequest);
        if(!questionValidationResult.first()){
            response.setData(questionValidationResult.second());
            response.setMessage("Invalid data provided");
            return  response;
        }

        String questionId=UUID.randomUUID().toString();
        boolean questionDirectoryCreation=fileSystemHelper.createQuestionDirectory(questionId);
        if(!questionDirectoryCreation){
            response.setMessage("Unable to create directory");
            response.setData("Unable to create directory for question");
            return response;
        }

        ValidatorResult<Boolean, Path> extractionResult =
                fileSystemHelper.extractZipToTemporary(
                        questionCreateRequest.getTest_cases(),
                        questionId
                );

        if(!extractionResult.first()){
            response.setMessage("Extraction failed");
            response.setData("Unable to extract testcases");
            return response;
        }

        Path tempDir = extractionResult.second();

        int testcaseCount = fileSystemHelper.countTestCases(tempDir);
        questionRepository.save(
                new QuestionModel(
                        questionId,
                        questionCreateRequest.getTitle(),
                        questionCreateRequest.getMarkdown(),
                        basePath+questionId+"/testcases",
                        testcaseCount,
                        0,
                        0,
                        (int)questionCreateRequest.getTime_limit()*1000,
                        questionCreateRequest.getMemory_limit(),
                        questionCreateRequest.getTopics(),
                        QuestionModel.Difficulty.valueOf(questionCreateRequest.getDifficulty())
                )
        );
        // Saving to db done now move from temp to real location
        boolean moveResult = fileSystemHelper.moveTempToQuestionDirectory(tempDir, questionId);

        if(!moveResult){
            questionRepository.deleteById(questionId);

            response.setMessage("Failed to move testcases");
            response.setData("Filesystem error while moving extracted files");
            return response;
        }
        response.setSuccess(true);
        response.setMessage("Question created successfully");
        response.setData(questionId);

        return response;
    }

    public QuestionFetchResponse fetchQuestion(String id) {
        return new QuestionFetchResponse(questionRepository.findById(id).orElse(null));
    }

    public QuestionFetchAllResponse fetchAllQuestion() {
        Map<String, String> response = new java.util.HashMap<>(Map.of());
        questionRepository.findAll().forEach(question -> {response.put(question.getQuestionId(), question.getTitle());});
        return new QuestionFetchAllResponse(response);
    }
}
