package com.varshith.coderunner_workers.dispatcher;


import com.varshith.coderunner_workers.executors.CodeExecutor;
import com.varshith.coderunner_workers.models.SubmissionModel;
import com.varshith.coderunner_workers.repository.SubmissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SubmissionDispatcher {

    private final Map<String, CodeExecutor> codeExecutors;
    private final SubmissionRepository submissionRepository;

    public SubmissionDispatcher(List<CodeExecutor> codeExecutors,  SubmissionRepository submissionRepository) {
        this.codeExecutors = codeExecutors.stream()
                .collect(Collectors.toMap(CodeExecutor::getLanguage, Function.identity()));

        this.submissionRepository = submissionRepository;
    }
    // Dispatcher fetches data from database and sends the request to corresponding executor class
    public boolean dispatch(String submissionId){
        Long submissionIdLong= Long.parseLong(submissionId);

        SubmissionModel submission=submissionRepository.findById(submissionIdLong).orElse(null);

        if(submission==null){
            log.info("Submission id {} not found", submissionId);
            return;
        }

        String language=submission.getLanguage().getLanguageName();
        CodeExecutor codeExecutor=codeExecutors.get(language);

        if(codeExecutor==null){
            log.info("No code executor found for language {}",language);
            return;
        }

        return codeExecutor.execute(submission);

    }


}
