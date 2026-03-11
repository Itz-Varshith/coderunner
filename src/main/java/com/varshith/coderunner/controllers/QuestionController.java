package com.varshith.coderunner.controllers;


import com.varshith.coderunner.dtos.APIResponse;
import com.varshith.coderunner.dtos.QuestionCreateRequest;
import com.varshith.coderunner.dtos.QuestionCreateResponse;
import com.varshith.coderunner.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;


    @GetMapping("/get/{id}")
    public QuestionCreateResponse getQuestionFromId(@PathVariable String id){
        return new QuestionCreateResponse();
    }

    @PostMapping("/create-question")
    public ResponseEntity<APIResponse<String>> createQuestion(@ModelAttribute QuestionCreateRequest questionCreateRequest){
        APIResponse<String> response=questionService.createQuestion(questionCreateRequest);
        if(!response.isSuccess()){
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>
                (response , HttpStatus.CREATED);

    }

    @GetMapping("/get-stats/{id}")
    public QuestionCreateResponse getQuestionStats(@PathVariable String id){
        return new QuestionCreateResponse();
    }
}
