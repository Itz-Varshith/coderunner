package com.varshith.coderunner_workers.executors.bash_executors;

import com.varshith.coderunner_workers.executors.DockerExecutor;
import com.varshith.coderunner_workers.helpers.PrepareScript;
import com.varshith.coderunner_workers.models.SubmissionModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@RequiredArgsConstructor
public class PythonExecutorBash implements CodeExecutorBash {

    private final DockerExecutor dockerExecutor;
    private final PrepareScript prepareScript;

    @Override
    public String getLanguage(){
        return "python";
    }

    @Override
    public boolean execute(SubmissionModel submission){
        log.info("Executing code executor for language {}", getLanguage());

        String prefixForTempDirectory = "coderunner-python-" + submission.getSubmissionId() + "-";
        Path tempDirectory = null;

        try {
            // Step 1: Workspace creation
            Path baseTempDir = Paths.get(System.getProperty("user.dir"), "coderunner_workspaces");
            if (!Files.exists(baseTempDir)) {
                Files.createDirectories(baseTempDir);
            }
            tempDirectory = Files.createTempDirectory(baseTempDir, prefixForTempDirectory);
            log.info("Created temp workspace at: {}", tempDirectory.toAbsolutePath());

            // Check Testcases
            String testcasesPath = submission.getQuestion().getTestcasesPath();
            if(testcasesPath == null){
                log.error("No testcases found, denied execution");
                return false;
            }
            Path testCasesLocation = Paths.get(testcasesPath);
            if(!Files.exists(testCasesLocation)){
                log.error("Testcases for question {} do not exist on device", submission.getQuestion().getQuestionId());
                return false;
            }

            // Step 1.5: Write user code
            String userFileName = "solution.py";
            Path userCodeFile = tempDirectory.resolve(userFileName);
            Files.writeString(userCodeFile, submission.getCode());

            // Step 2: Prepare Script
            String script = prepareScript.makeScript("python_run.sh", submission);


            Path runScriptFile = tempDirectory.resolve("run.sh");
            Files.writeString(runScriptFile, script);

            // Step 3 & 4: Execute via Docker
            String result = dockerExecutor.dockerExecute(tempDirectory, testCasesLocation, "judge-python");
            log.info("Done execution for Python");
            return true;

        } catch (Exception err) {
            log.error("Execution setup failed for Python", err);
            return false;
        }
    }
}