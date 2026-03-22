package com.varshith.coderunner_workers.executors.bash_executors;


import com.varshith.coderunner_workers.executors.DockerExecutor;
import com.varshith.coderunner_workers.helpers.PrepareScript;
import com.varshith.coderunner_workers.models.SubmissionModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor
public class CppExecutorBash implements CodeExecutorBash {

    private final DockerExecutor dockerExecutor;
    private final PrepareScript prepareScript;


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
        //        Step - 1
        String prefixForTempDirectory = "coderunner-" + submission.getSubmissionId() + "-";
        Path tempDirectory;
        try {
            // Get the current working directory of the Java application
            Path baseTempDir = Paths.get(System.getProperty("user.dir"), "coderunner_workspaces");

            // Ensure this base directory exists
            if (!Files.exists(baseTempDir)) {
                Files.createDirectories(baseTempDir);
            }

            // Create the specific temp dir INSIDE our dedicated folder instead of the OS /tmp
            tempDirectory = Files.createTempDirectory(baseTempDir, prefixForTempDirectory);
            log.info("Created temp workspace at: {}", tempDirectory.toAbsolutePath());

        } catch (IOException err) {
            log.error("Temporary directory creation error", err);
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

//         Step 1.5
        String code = submission.getCode();

        Path userCodeFile = tempDirectory.resolve("user_code.cpp");

        try {
            Files.writeString(userCodeFile, code);
        } catch (IOException err) {
            log.error("Failed to write user code", err);
            return false;
        }
//        Step 2
        String script=prepareScript.makeScript("cpp_run.sh", submission);
        Path runScriptFile = tempDirectory.resolve("run.sh");

        try {
            Files.writeString(runScriptFile, script);
        } catch (IOException err) {
            log.error("Failed to write Run script", err);
            return false;
        }
//        Mention the improvement related to avoiding file copy and directly mounting the testcases path to the docker container
//        Pass the directory along with the image name to the docker executor.
        String result=dockerExecutor.dockerExecute(tempDirectory,testCasesLocation,  "judge-cpp");
        log.info("Done execution");
        return true;
    }
}
