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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

/*
* Question Service class main entry point for the creation of questions, takes many variables ensures every required value is set.
* */

// I recently learnt that required args constructor directly converts service injections into constructor based ones.
@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    @Value("${spring.testcases.base_path}")
    private String basePath;


    private final QuestionValidator questionValidator;
    private final FileSystemHelper fileSystemHelper;
    private final QuestionRepository questionRepository;
    private final RedisTemplate<String, Object> cacheRedisTemplate;

    private static final Duration QUESTION_TTL = Duration.ofMinutes(10);
    private static final String ALL_QUESTIONS_KEY = "questions:all";

    private static String questionKey(String id) {
        return "question:" + id;
    }



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
                        QuestionModel.Difficulty.valueOf(questionCreateRequest.getDifficulty()),
                        questionCreateRequest.isCustomJudge()
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
        // Invalidate the cached question list so the new question appears immediately.
        try {
            cacheRedisTemplate.delete(ALL_QUESTIONS_KEY);
        } catch (Exception e) {
            log.warn("Failed to evict cached question list after creating {}", questionId, e);
        }

        response.setSuccess(true);
        response.setMessage("Question created successfully");
        response.setData(questionId);

        return response;
    }

    public QuestionFetchResponse fetchQuestion(String id) {
        String cacheKey = questionKey(id);

        // Cache hit: serve the question directly from Redis.
        Object cached = cacheRedisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof QuestionModel question) {
            log.info("Question {} served from Redis cache", id);
            return new QuestionFetchResponse(question);
        }

        // Cache miss: load from the database and warm the cache.
        QuestionModel question = questionRepository.findById(id).orElse(null);
        if (question != null) {
            try {
                cacheRedisTemplate.opsForValue().set(cacheKey, question, QUESTION_TTL);
                log.info("Question {} loaded from database and cached", id);
            } catch (Exception e) {
                log.warn("Failed to cache question {}", id, e);
            }
        }
        return new QuestionFetchResponse(question);
    }

    @SuppressWarnings("unchecked")
    public List<QuestionFetchAllResponse> fetchAllQuestion() {
        // Cache hit: serve the full question list directly from Redis.
        Object cached = cacheRedisTemplate.opsForValue().get(ALL_QUESTIONS_KEY);
        if (cached instanceof List<?> cachedList) {
            log.info("All questions served from Redis cache");
            return (List<QuestionFetchAllResponse>) cachedList;
        }

        List<QuestionModel> questions = questionRepository.findAll();
        List<QuestionFetchAllResponse> response = new ArrayList<>();
        for (QuestionModel question : questions) {
            double acceptanceRate = question.getSubmissions() > 0 
                ? (double) question.getAccepted() / question.getSubmissions() * 100 
                : 0.0;
            response.add(new QuestionFetchAllResponse(
                question.getQuestionId(),
                question.getTitle(),
                acceptanceRate,
                question.getDifficulty().toString()
            ));
        }

        try {
            cacheRedisTemplate.opsForValue().set(ALL_QUESTIONS_KEY, response, QUESTION_TTL);
            log.info("All questions loaded from database and cached");
        } catch (Exception e) {
            log.warn("Failed to cache all questions", e);
        }
        return response;
    }
}
