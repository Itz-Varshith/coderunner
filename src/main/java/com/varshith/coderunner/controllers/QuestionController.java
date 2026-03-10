package com.varshith.coderunner.controllers;


import com.varshith.coderunner.dtos.QuestionResponse;
import jakarta.ws.rs.Path;
import org.aspectj.weaver.patterns.TypePatternQuestions;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/question")
public class QuestionController {

    @GetMapping("/get/{id}")
    public QuestionResponse getQuestionFromId(@PathVariable String id){
        return new QuestionResponse();
    }

    @GetMapping("/get-stats/{id}")
    public QuestionResponse getQuestionStats(@PathVariable String id){
        return new QuestionResponse();
    }
}
