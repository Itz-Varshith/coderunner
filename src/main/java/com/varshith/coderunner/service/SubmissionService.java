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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {

    private final SubmissionValidator submissionValidator;
    private final SubmissionRepository submissionRepository;
    private final LanguageRepository languageRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

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
        response.setData("");
        return response;
    }
}
