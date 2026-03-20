package com.varshith.coderunner_workers.dispatcher;


import com.github.dockerjava.api.DockerClient;
import com.varshith.coderunner_workers.executors.DockerExecutor;
import com.varshith.coderunner_workers.models.SubmissionModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class JudgeBootstrapCompiler {


    private final DockerExecutor dockerExecutor;
    private final Set<String> compilingQuestions = ConcurrentHashMap.newKeySet();


    private boolean waitForJudgeBinary(Path judgeBinary) {
        int attempts = 0;
        while (attempts < 15 && !Files.exists(judgeBinary)) {
            try {
                Thread.sleep(500); // Check every 0.5 seconds
                attempts++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return Files.exists(judgeBinary);
    }

    public boolean bootstrapJudgeProgram(SubmissionModel submission){
        // 1. Identify the paths
        String testcasesPathStr = submission.getQuestion().getTestcasesPath();
        Path testcasesPath = Paths.get(testcasesPathStr);
        Path judgeBinary = testcasesPath.resolve("judge_program");
        String questionId = submission.getQuestion().getQuestionId().toString();

        // 2. Check if it already exists (The "Fast Path")
        if (Files.exists(judgeBinary)) {
            log.info("Judge binary exists for question {}, skipping compilation.", questionId);
        } else {
            // 3. The "Slow Path" with a thread-safe lock
            // compilingQuestions is a: private final Set<String> compilingQuestions = ConcurrentHashMap.newKeySet();

            if (compilingQuestions.add(questionId)) {
                try {
                    log.info("First submission for question {}. Compiling judge...", questionId);

                    // Invoke the Docker compiler (Phase 1: Privileged Read/Write)
                    boolean success = dockerExecutor.compileJudge(testcasesPath);

                    if (!success) {
                        log.error("Failed to compile judge for question {}", questionId);
                        return false;
                    }
                } finally {
                    // Always remove from the set so future checks can run if needed
                    compilingQuestions.remove(questionId);
                }
            } else {
                // 4. Another thread is already compiling it. Wait for it to finish.
                if (!waitForJudgeBinary(judgeBinary)) {
                    log.error("Timed out waiting for judge binary to be created by another thread.");
                    return false;
                }
            }
        }
        return true;
    }

}
