package com.varshith.coderunner_workers.executors;


import com.varshith.coderunner_workers.models.SubmissionModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
public class CppExecutor implements CodeExecutor {

    public String getLanguage(){
        return "cpp";
    }

    public boolean execute(SubmissionModel submission){
        log.info("Executing code executor for language {}",getLanguage());
        // Sandboxing and all to be done here.

        /*
        * Steps:
        * 1) Create temp directory and paste all the testcases and judge(path for these will be provided by
        * question model that is in the Submission received) to that folder.
        * 1.5) Write user code into temp dir we just created
        * 2) Create and cook up a run.sh into the temp folder that gives all the commands to run inside the image
        * 3) Forward the request with the temp file path to the docker executor, it will spin up a container and give us
        *  something(we will take care of this later)
        * 4) Destroy the container within the DockerExecutor code and return the result to the execute function here
        * 5) Update DB regarding the result and return, then automatically ack is done which is already coded into the
        * pipeline.
        * 6) Delete the temp directory, from server machine
        * */

//        Step - 1
        String prefixForTempDirectory="coderunner-"+ submission.getSubmissionId();
        Path tempDirectory;
        try{
            tempDirectory = Files.createTempDirectory(prefixForTempDirectory);
        } catch (IOException err){
            log.error("Temporary directory creation error");
            return false;
        }

        String testcasesPath=submission.getQuestion().getTestcasesPath();
        if(testcasesPath==null){
            log.error("No testcases found denied execution");
            return false;
        }

        Path testCasesLocation= Paths.get(testcasesPath);

        if(!Files.exists(testCasesLocation)){
            log.error("Testcases for the question with id {} do not exist on device", submission.getQuestion().getQuestionId());
            return false;
        }
        // This approach for file copying is ok but can be easily optimized using simple docker mound directly instead of
        // using separate copy to temp like we are now.
        try{
            Files.walk(testCasesLocation)
                    .forEach(source -> {
                        Path dest = tempDirectory.resolve(testCasesLocation.relativize(source));
                        try {
                            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }catch(IOException err){
            log.error("File copy failed");
            return false;
        }

//         Steo 1.5
        String code = submission.getCode();

        Path userCodeFile = tempDirectory.resolve("user_code.cpp");

        try {
            Files.writeString(userCodeFile, code);
        } catch (IOException err) {
            log.error("Failed to write user code", err);
            return false;
        }

        log.info("Done execution");
        return true;
    }
}
