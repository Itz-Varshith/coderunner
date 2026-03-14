package com.varshith.coderunner_workers.executors;


import com.varshith.coderunner_workers.models.SubmissionModel;
import org.springframework.stereotype.Component;

@Component
public interface CodeExecutor {

    String getLanguage();
    void execute(SubmissionModel submission);

}
