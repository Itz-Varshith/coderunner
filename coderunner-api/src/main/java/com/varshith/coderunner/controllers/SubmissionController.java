package com.varshith.coderunner.controllers;


import com.varshith.coderunner.dtos.APIResponse;
import com.varshith.coderunner.dtos.SubmissionCreateRequest;
import com.varshith.coderunner.models.SubmissionModel;
import com.varshith.coderunner.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
* Main endpoint for submission creation and fetching submission details.
*
* Improvement that can be performed here is that currently the server does not have any sort of server sent events or socket established for quick relay of the test results or something, but in the future we can implement a clean SSE pattern here to have real time updates quick.
* */

@RestController
@RequestMapping("/submit")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
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


    @GetMapping("/all-submissions/{userId}")
    public ResponseEntity<List<SubmissionModel>> fetchAllSubmissions(@PathVariable String userId) {
        APIResponse<List<SubmissionModel>> response=submissionService.getAllSubmissions(userId);
        if(!response.isSuccess()) {
            return new ResponseEntity<>(response.getData(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(response.getData(), HttpStatus.OK);
    }
}
