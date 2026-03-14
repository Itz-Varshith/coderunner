package com.varshith.coderunner_workers.executors;


import com.varshith.coderunner_workers.models.SubmissionModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CppExecutor implements CodeExecutor {

    public String getLanguage(){
        return "cpp";
    }

    public void execute(SubmissionModel submission){
        log.info("Executing code executor for language {}",getLanguage());
        // Sandboxing and all to be done here.
        log.info("Done execution");
    }
}
