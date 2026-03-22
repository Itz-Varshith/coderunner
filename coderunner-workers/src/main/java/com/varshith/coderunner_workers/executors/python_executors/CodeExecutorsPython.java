package com.varshith.coderunner_workers.executors.python_executors;


import com.varshith.coderunner_workers.models.SubmissionModel;
import org.springframework.stereotype.Component;

@Component
public interface CodeExecutorsPython {
    String getLanguage();
    boolean execute(SubmissionModel submission);
}
