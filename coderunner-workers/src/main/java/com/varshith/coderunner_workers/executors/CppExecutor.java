package com.varshith.coderunner_workers.executors;


import com.varshith.coderunner_workers.models.SubmissionModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
@RequiredArgsConstructor
public class CppExecutor implements CodeExecutor {

    private final DockerExecutor dockerExecutor;



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
        String script = """
#!/bin/bash

USER_CODE="user_code.cpp"
JUDGE_CODE="judge.cpp"

USER_EXEC="user_program"
JUDGE_EXEC="judge_program"

TESTCASE_DIR="./input"

USER_OUTPUT="user_output.txt"
USER_ERROR="runtime_error.txt"

g++ "$USER_CODE" -O2 -std=c++17 -o "$USER_EXEC"
if [ $? -ne 0 ]; then
    echo "USER_COMPILATION_ERROR"
    exit 1
fi

g++ "$JUDGE_CODE" -O2 -std=c++17 -o "$JUDGE_EXEC"
if [ $? -ne 0 ]; then
    echo "JUDGE_COMPILATION_ERROR"
    exit 1
fi

for testcase in "$TESTCASE_DIR"/*.txt
do
    ./"$USER_EXEC" < "$testcase" > "$USER_OUTPUT" 2> "$USER_ERROR"

    STATUS=$?

    if [ $STATUS -ne 0 ]; then
        echo "RUNTIME_ERROR"
        exit 1
    fi

    ./"$JUDGE_EXEC" "$testcase" < "$USER_OUTPUT"

    if [ $? -ne 0 ]; then
        echo "WRONG_ANSWER"
        exit 1
    fi
done

echo "ACCEPTED"
""";

        Path runScriptFile = tempDirectory.resolve("run.sh");

        try {
            Files.writeString(runScriptFile, script);
        } catch (IOException err) {
            log.error("Failed to write Run script", err);
            return false;
        }

//        Pass the directory along with the image name to the docker executor.
        String result=dockerExecutor.dockerExecute(tempDirectory, "judge-cpp");
        log.info("Done execution");
        return true;
    }
}
