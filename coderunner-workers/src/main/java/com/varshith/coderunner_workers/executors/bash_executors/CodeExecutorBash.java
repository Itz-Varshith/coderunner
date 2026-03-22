package com.varshith.coderunner_workers.executors.bash_executors;


import com.varshith.coderunner_workers.models.SubmissionModel;
import org.springframework.stereotype.Component;

@Component
public interface CodeExecutorBash {

    String getLanguage();
    boolean execute(SubmissionModel submission);

}
