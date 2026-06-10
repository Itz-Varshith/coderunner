package com.varshith.coderunner.service;


import com.varshith.coderunner.dtos.APIResponse;
import com.varshith.coderunner.dtos.SubmissionCreateRequest;
import com.varshith.coderunner.helpers.ValidatorResult;
import com.varshith.coderunner.helpers.SubmissionValidator;


import com.varshith.coderunner.models.LanguageModel;
import com.varshith.coderunner.models.QuestionModel;
import com.varshith.coderunner.models.SubmissionModel;
import com.varshith.coderunner.models.UserModel;
import com.varshith.coderunner.repository.LanguageRepository;
import com.varshith.coderunner.repository.QuestionRepository;
import com.varshith.coderunner.repository.SubmissionRepository;
import com.varshith.coderunner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j

/*
* Submission Service class does all the work of creating submissions and  persisting them to database and enqueueing them to redist stream for checking.
* */

public class SubmissionService {

    private final SubmissionValidator submissionValidator;
    private final SubmissionRepository submissionRepository;
    private final LanguageRepository languageRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<Object, Object> objectRedisTemplate;
    private final RedisTemplate<String, Object> cacheRedisTemplate;

    private static final Duration USER_SUBMISSIONS_TTL = Duration.ofMinutes(5);

    private static String userSubmissionsKey(String userId) {
        return "submissions:user:" + userId;
    }

    public APIResponse<String> createSubmission(SubmissionCreateRequest submissionCreateRequest) {
        APIResponse<String> response = new APIResponse<>();
        response.setSuccess(false);
        response.setDate(new Date());
        /*
        * Steps
        * 1) Validate the submission based on question id, language existence and code file non empty
        * 2) Create submission in database
        * 3) Enqueue the request into the corresponding language stream and return.
        * */
        //  Step - 1
        ValidatorResult<Boolean, String> validationResult=submissionValidator.validateSubmission(submissionCreateRequest);
        if(!validationResult.first()){
            response.setMessage("Invalid submission");
            response.setData(validationResult.second());
            return response;
        }
        log.info("Submission validation successful");

        // Step - 2
        LanguageModel language=languageRepository.findByLanguageName(submissionCreateRequest.getLanguage().trim());
        QuestionModel question=questionRepository.findById(submissionCreateRequest.getQuestionId().trim()).get();
        UserModel user= userRepository.findById(submissionCreateRequest.getUserId().trim()).get();

        SubmissionModel submissionModel = new SubmissionModel();
        submissionModel.setLanguage(language);
        submissionModel.setQuestion(question);
        submissionModel.setUser(user);
        submissionModel.setCode(submissionCreateRequest.getCode().trim());
        submissionModel.setStatus(SubmissionModel.Status.PENDING);

        SubmissionModel saved = submissionRepository.save(submissionModel);
        log.info("Submission {} saved to database", saved.getSubmissionId());

        // Invalidate the user's cached submission list so the new submission shows up immediately.
        try {
            cacheRedisTemplate.delete(userSubmissionsKey(submissionCreateRequest.getUserId().trim()));
        } catch (Exception e) {
            log.warn("Failed to evict cached submissions for user {}", submissionCreateRequest.getUserId(), e);
        }

        // Step - 3
        Map<String, String> message=new HashMap<>();
        message.put("submissionId", String.valueOf(saved.getSubmissionId()));
        redisTemplate.opsForStream().add(
                "submission-stream",
                message
        );
        log.info("Submission {} added to stream {}",
                (int)saved.getSubmissionId(),
                "submission-stream");
        response.setMessage("Submission successful");
        response.setSuccess(true);
        response.setData(""+saved.getSubmissionId());
        return response;
    }

    public SubmissionModel fetchSubmission(String id) {
        Long idLong = Long.parseLong(id);

        // Cache hit: the template deserializes straight into a SubmissionModel.
        Object cached = objectRedisTemplate.opsForValue().get(id);
        if (cached instanceof SubmissionModel submission) {
            log.info("Submission {} served from Redis cache", id);
            return submission;
        }

        // Cache miss: load from the database and warm the cache for subsequent reads.
        SubmissionModel submission = submissionRepository.findById(idLong).orElse(null);
        if (submission != null) {
            try {
                objectRedisTemplate.opsForValue().set(id, submission);
                log.info("Submission {} loaded from database and cached", id);
            } catch (Exception e) {
                log.warn("Failed to cache submission {} after database fetch", id, e);
            }
        }

        return submission;
    }

    @SuppressWarnings("unchecked")
    public APIResponse<List<SubmissionModel>> getAllSubmissions(String userId) {
        APIResponse<List<SubmissionModel>> response = new APIResponse<>();

        response.setSuccess(false);
        response.setDate(new Date());

        String cacheKey = userSubmissionsKey(userId);

        // Cache hit: serve the user's submission list directly from Redis.
        Object cached = cacheRedisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof List<?> cachedList) {
            response.setData((List<SubmissionModel>) cachedList);
            response.setSuccess(true);
            log.info("Submissions for user {} served from Redis cache", userId);
            return response;
        }

        // Cache miss: fetch the latest 50 from the database and warm the cache.
        List<SubmissionModel> res = submissionRepository.findTop50ByUserIdOrderBySubmittedAtDesc(userId);
        try {
            cacheRedisTemplate.opsForValue().set(cacheKey, res, USER_SUBMISSIONS_TTL);
            log.info("Submissions for user {} loaded from database and cached", userId);
        } catch (Exception e) {
            log.warn("Failed to cache submissions for user {}", userId, e);
        }

        response.setData(res);
        response.setSuccess(true);
        return response;
    }
}
