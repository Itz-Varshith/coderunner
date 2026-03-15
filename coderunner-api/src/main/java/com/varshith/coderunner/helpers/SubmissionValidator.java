package com.varshith.coderunner.helpers;


import com.varshith.coderunner.dtos.SubmissionCreateRequest;
import com.varshith.coderunner.repository.LanguageRepository;
import com.varshith.coderunner.repository.QuestionRepository;
import com.varshith.coderunner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SubmissionValidator {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;

    public ValidatorResult<Boolean, String> validateSubmission(SubmissionCreateRequest submissionCreateRequest) {

        String language = submissionCreateRequest.getLanguage();
        String code = submissionCreateRequest.getCode();
        String userId = submissionCreateRequest.getUserId();
        String questionId = submissionCreateRequest.getQuestionId();

        if(language == null || code == null || userId == null || questionId == null){
            return new ValidatorResult<>(false, "Required fields missing");
        }

        language = language.trim();
        code = code.trim();
        userId = userId.trim();
        questionId = questionId.trim();


//      Empty check
        if(questionId.isBlank() || userId.isBlank() || code.isBlank() || language.isBlank()){
            return new ValidatorResult<>(false, "Required fields missing");
        }

//      Code length check
        if(code.length()>100_000){
            return new ValidatorResult<>(false, "Code too long");
        }

//      Question existence check
        if(!questionRepository.existsById(questionId)){
            return new ValidatorResult<>(false, "Question not found");
        }

//      User existence check
        if(!userRepository.existsById(userId)){
            return new ValidatorResult<>(false, "User not found");
        }

//      Language Existence check
        if(!languageRepository.existsByLanguageName(language)){
            return new ValidatorResult<>(false, "Language not in database");
        }

        return new ValidatorResult<>(true, "Submission validation successful");
    }
}
