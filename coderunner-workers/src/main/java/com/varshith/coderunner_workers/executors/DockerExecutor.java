package com.varshith.coderunner_workers.executors;


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.varshith.coderunner_workers.config.DockerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@RequiredArgsConstructor
@Slf4j
public class DockerExecutor {

    private final DockerClient dockerClient;

    public boolean compileJudge(Path testcasesDir) {
        try {
            log.info("Compiling judge binary in directory: {}", testcasesDir);


            CreateContainerResponse container = dockerClient.createContainerCmd("judge-cpp")
                    .withCmd("/bin/bash", "-c", "g++ /testcases/judge.cpp -O2 -std=c++17 -o /testcases/judge_program")
                    .withHostConfig(
                            HostConfig.newHostConfig()
                                    .withBinds(new Bind(testcasesDir.toString(), new Volume("/testcases")))
                    )
                    .exec();

            dockerClient.startContainerCmd(container.getId()).exec();


            int statusCode = dockerClient.waitContainerCmd(container.getId())
                    .start()
                    .awaitStatusCode();

            // Cleanup the compiler container
            dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();

            if (statusCode != 0) {
                log.error("Judge compilation failed with exit code: {}", statusCode);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Docker error during judge compilation", e);
            return false;
        }
    }


    public String dockerExecute(Path directory, Path testcasesPath, String image_name){

        try {
            dockerClient.pingCmd().exec();
        } catch (Exception e) {
            log.error("Docker is not running!");
            return "";
        }

        CreateContainerResponse container =
                dockerClient.createContainerCmd(image_name)
                        .withCmd("/bin/bash", "run.sh")
                        .withWorkingDir("/workspace")
                        .withHostConfig(
                                HostConfig.newHostConfig()
                                        .withBinds(new Bind(directory.toString(), new Volume("/workspace")),
                                                new Bind(testcasesPath.toString(), new Volume("/workspace/testcase"), AccessMode.ro))

                        )
                        .exec();
        dockerClient.startContainerCmd(container.getId()).exec();
        LogContainerCmd logCmd =
                dockerClient.logContainerCmd(container.getId())
                        .withStdOut(true)
                        .withStdErr(true)
                        .withFollowStream(true);
        logCmd.exec(new LogContainerResultCallback() {
            @Override
            public void onNext(Frame frame) {
                String log = new String(frame.getPayload());
                System.out.print(log);
            }
        });
        dockerClient.waitContainerCmd(container.getId()).start().awaitStatusCode();
        dockerClient.removeContainerCmd(container.getId())
                .withForce(true)
                .exec();
        return "Well! we are done";
    }

}


