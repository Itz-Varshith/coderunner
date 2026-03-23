package com.varshith.coderunner_workers.dispatcher;


import com.varshith.coderunner_workers.executors.python_executors.CodeExecutorsPython;
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
public class SubmissionDispatcherPython {

    private final Map<String, CodeExecutorsPython> codeExecutors;
    private final SubmissionRepository submissionRepository;
    private final JudgeBootstrapCompiler judgeBootstrapCompiler;


    public SubmissionDispatcherPython(List<CodeExecutorsPython> codeExecutorBashes, SubmissionRepository submissionRepository, JudgeBootstrapCompiler judgeBootstrapCompiler) {
        this.codeExecutors = codeExecutorBashes.stream()
                .collect(Collectors.toMap(CodeExecutorsPython::getLanguage, Function.identity()));

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
        CodeExecutorsPython codeExecutorsPython =codeExecutors.get(language);

        if(codeExecutorsPython==null){
            log.info("No code executor found for language {}",language);
            return false;
        }



        boolean judgeBootStrapResult= judgeBootstrapCompiler.bootstrapJudgeProgram(submission);
        if(!judgeBootStrapResult){
            log.error("Judge program could not be bootstrapped");
            return false;
        }

        boolean res= codeExecutorsPython.execute(submission);
        if(res){
            submissionRepository.save(submission);
        }
        return res;
    }


}
