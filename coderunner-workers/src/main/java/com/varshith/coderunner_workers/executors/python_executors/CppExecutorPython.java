package com.varshith.coderunner_workers.executors.python_executors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.varshith.coderunner_workers.executors.DockerExecutor;
import com.varshith.coderunner_workers.models.SubmissionModel;
import com.varshith.coderunner_workers.repository.SubmissionRepository;
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
    // 1. Inject ObjectMapper to parse the JSON file easily
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            String checkermode=submission.getQuestion().isCustomJudge()?"custom":"standard";
            String command =
                    "python3 -u run.py " +
                            "\"" + compileCmd + "\" " +
                            "\"" + runCmd + "\" " +
                            "\"" + judgeCmd + "\" " +
                            timeLimitMs + " " +
                            memoryLimitMb+ " "+ 
                            checkermode;

            String result = dockerExecutor.dockerExecutePython(tempDirectory, testCasesLocation, "judge-cpp-python", command);
            log.info("Done execution, attempting to read result.json");

            // 2. Logic to read result from result.json file
            Path resultFilePath = tempDirectory.resolve("result.json");

            // Safety check: Did the Python script crash completely before writing the file?
            if (!Files.exists(resultFilePath)) {
                log.error("result.json not found! The Docker container might have crashed or the Python script failed to execute properly.");
                submission.setStatus(SubmissionModel.Status.PENDING);
                submission.setJudgeMessage("System error: Executor failed to produce a result.");
                return false;
            }

            // Parse the JSON directly into a JsonNode object
            JsonNode resultNode = objectMapper.readTree(resultFilePath.toFile());

            boolean isSystemError = resultNode.path("isSystemError").asBoolean(true);
            String status = resultNode.path("status").asText("SYSTEM_ERROR");
            String judgeMessage = resultNode.path("judgeMessage").asText("Unknown Error");
            int timeTakenMs = resultNode.path("timeTakenMs").asInt(0);
            int memoryTakenKb = resultNode.path("memoryTakenKb").asInt(0);

            submission.setStatus(SubmissionModel.Status.valueOf(status));
            submission.setJudgeMessage(judgeMessage);
            submission.setTimeTaken(timeTakenMs);
            submission.setMemoryTaken(memoryTakenKb);

            if (isSystemError) {
                log.error("Execution resulted in a system error: {}", judgeMessage);
                return false;
            }

            log.info("Successfully parsed result: Status={}, Time={}ms, Memory={}KB", status, timeTakenMs, memoryTakenKb);

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