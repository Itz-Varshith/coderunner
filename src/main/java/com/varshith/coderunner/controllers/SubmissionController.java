package com.varshith.coderunner.controllers;


import com.varshith.coderunner.dtos.APIResponse;
import com.varshith.coderunner.dtos.SubmissionCreateRequest;
import com.varshith.coderunner.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/submit")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<APIResponse<String>> submitCode(@RequestBody SubmissionCreateRequest submissionCreateRequest) {

        APIResponse<String> response=submissionService.createSubmission(submissionCreateRequest);
        if(!response.isSuccess()) {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return new  ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}
