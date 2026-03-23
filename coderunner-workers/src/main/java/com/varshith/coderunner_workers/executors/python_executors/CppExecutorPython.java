package com.varshith.coderunner_workers.executors.python_executors;

import com.varshith.coderunner_workers.executors.DockerExecutor;
import com.varshith.coderunner_workers.models.SubmissionModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
@RequiredArgsConstructor
public class CppExecutorPython implements CodeExecutorsPython {

    private final DockerExecutor dockerExecutor;

    public String getLanguage(){
        return "cpp";
    }

    public boolean execute(SubmissionModel submission){
        Path tempDirectory = null;

        try {
            log.info("Executing code executor for language {}", getLanguage());

            String prefixForTempDirectory = "coderunner-" + submission.getSubmissionId() + "-";

            try {
                Path baseTempDir = Paths.get(System.getProperty("user.dir"), "coderunner_workspaces");
                if (!Files.exists(baseTempDir)) {
                    Files.createDirectories(baseTempDir);
                }
                tempDirectory = Files.createTempDirectory(baseTempDir, prefixForTempDirectory);
                log.info("Created temp workspace at: {}", tempDirectory.toAbsolutePath());

            } catch (IOException err) {
                log.error("Temporary directory creation error", err);
                return false;
            }

            String testcasesPath = submission.getQuestion().getTestcasesPath();
            if (testcasesPath == null) {
                log.error("No testcases found denied execution");
                return false;
            }
            Path testCasesLocation = Paths.get(testcasesPath);

            if (!Files.exists(testCasesLocation)) {
                log.error("Testcases for the question with id {} do not exist on device", submission.getQuestion().getQuestionId());
                return false;
            }

            String code = submission.getCode();
            Path userCodeFile = tempDirectory.resolve("user_code.cpp");

            try {
                Files.writeString(userCodeFile, code);
            } catch (IOException err) {
                log.error("Failed to write user code", err);
                return false;
            }

            Path runScriptFile = tempDirectory.resolve("run.py");
            InputStream is = getClass().getClassLoader().getResourceAsStream("python_scripts/run.py");
            String script = "";

            try {
                if (is == null) {
                    log.info("Script file for cpp not found");
                    return false;
                }
                script = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException err) {
                log.error("Failed to read script", err);
                return false;
            }

            try {
                Files.writeString(runScriptFile, script);
            } catch (IOException err) {
                log.error("Failed to write Run script", err);
                return false;
            }

            String compileCmd = "g++ user_code.cpp -O2 -o user_program";
            String runCmd = "./user_program";
            String judgeCmd = "./testcase/judge_program";

            int timeLimitMs = submission.getQuestion().getTimeLimit();
            int memoryLimitMb = submission.getQuestion().getMemoryLimit();

            String command =
                    "python3 -u run.py " +
                            "\"" + compileCmd + "\" " +
                            "\"" + runCmd + "\" " +
                            "\"" + judgeCmd + "\" " +
                            timeLimitMs + " " +
                            memoryLimitMb;

            String result = dockerExecutor.dockerExecutePython(tempDirectory, testCasesLocation, "judge-cpp-python", command);
            log.info("Done execution");
            return true;

        } catch(Exception err){
            log.error("Error while executing code: {}", err.getMessage());
            return false;

        } finally {
            if (tempDirectory != null) {
                try {
                    FileSystemUtils.deleteRecursively(tempDirectory);
                    log.info("Cleaned up workspace: {}", tempDirectory);
                } catch (IOException e) {
                    log.error("CRITICAL: Failed to delete temporary directory! Disk leak possible at: {}", tempDirectory, e);
                }
            }
        }
    }
}