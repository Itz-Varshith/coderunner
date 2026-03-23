package com.varshith.coderunner.controllers;


import com.varshith.coderunner.dtos.APIResponse;
import com.varshith.coderunner.dtos.SubmissionCreateRequest;
import com.varshith.coderunner.models.SubmissionModel;
import com.varshith.coderunner.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/submit")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
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

    @GetMapping("/details/{id}")
    public SubmissionModel fetchSubmissionDetails(@PathVariable String id) {
        return submissionService.fetchSubmission(id);
    }
}
