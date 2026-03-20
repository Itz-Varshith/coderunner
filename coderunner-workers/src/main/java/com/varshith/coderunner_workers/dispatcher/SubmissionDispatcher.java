package com.varshith.coderunner_workers.dispatcher;


import com.varshith.coderunner_workers.executors.CodeExecutor;
import com.varshith.coderunner_workers.models.SubmissionModel;
import com.varshith.coderunner_workers.repository.SubmissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SubmissionDispatcher {

    private final Map<String, CodeExecutor> codeExecutors;
    private final SubmissionRepository submissionRepository;
    private final JudgeBootstrapCompiler judgeBootstrapCompiler;


    public SubmissionDispatcher(List<CodeExecutor> codeExecutors,  SubmissionRepository submissionRepository, JudgeBootstrapCompiler judgeBootstrapCompiler) {
        this.codeExecutors = codeExecutors.stream()
                .collect(Collectors.toMap(CodeExecutor::getLanguage, Function.identity()));

        this.submissionRepository = submissionRepository;
        this.judgeBootstrapCompiler= judgeBootstrapCompiler;
    }




    // Dispatcher fetches data from database and sends the request to corresponding executor class
    public boolean dispatch(String submissionId){
        Long submissionIdLong= Long.parseLong(submissionId);

        SubmissionModel submission=submissionRepository.findById(submissionIdLong).orElse(null);

        if(submission==null){
            log.info("Submission id {} not found", submissionId);
            return false;
        }

        String language=submission.getLanguage().getLanguageName();
        CodeExecutor codeExecutor=codeExecutors.get(language);

        if(codeExecutor==null){
            log.info("No code executor found for language {}",language);
            return false;
        }



        boolean judgeBootStrapResult= judgeBootstrapCompiler.bootstrapJudgeProgram(submission);
        if(!judgeBootStrapResult){
            log.error("Judge program could not be bootstrapped");
            return false;
        }

        return codeExecutor.execute(submission);

    }


}
