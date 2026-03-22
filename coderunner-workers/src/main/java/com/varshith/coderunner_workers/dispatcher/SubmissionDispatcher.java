package com.varshith.coderunner_workers.dispatcher;


import com.varshith.coderunner_workers.executors.bash_executors.CodeExecutorBash;
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

    private final Map<String, CodeExecutorBash> codeExecutors;
    private final SubmissionRepository submissionRepository;
    private final JudgeBootstrapCompiler judgeBootstrapCompiler;


    public SubmissionDispatcher(List<CodeExecutorBash> codeExecutorBashes, SubmissionRepository submissionRepository, JudgeBootstrapCompiler judgeBootstrapCompiler) {
        this.codeExecutors = codeExecutorBashes.stream()
                .collect(Collectors.toMap(CodeExecutorBash::getLanguage, Function.identity()));

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
        CodeExecutorBash codeExecutorBash =codeExecutors.get(language);

        if(codeExecutorBash ==null){
            log.info("No code executor found for language {}",language);
            return false;
        }



        boolean judgeBootStrapResult= judgeBootstrapCompiler.bootstrapJudgeProgram(submission);
        if(!judgeBootStrapResult){
            log.error("Judge program could not be bootstrapped");
            return false;
        }

        return codeExecutorBash.execute(submission);

    }


}
