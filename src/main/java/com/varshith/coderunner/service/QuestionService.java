package com.varshith.coderunner.service;


import com.varshith.coderunner.dtos.APIResponse;
import com.varshith.coderunner.dtos.QuestionCreateRequest;
import com.varshith.coderunner.helpers.QuestionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.Date;


// I recently learnt that required args constructor directly converts service injections into constructor based ones.
@Service
@RequiredArgsConstructor
public class QuestionService {

    private QuestionValidator questionValidator;

    public APIResponse<String> createQuestion(QuestionCreateRequest questionCreateRequest) {
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
        if(!questionValidator.validateQuestionData(questionCreateRequest)){
            response.setSuccess(false);
            response.setData("Invalid data provided");
            response.setMessage("Invalid data provided");
            response.setDate(new Date());
            return  response;
        }
        return response;
    }
}
