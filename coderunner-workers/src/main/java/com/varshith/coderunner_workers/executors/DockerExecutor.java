package com.varshith.coderunner_workers.executors;


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.varshith.coderunner_workers.config.DockerConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class DockerExecutor {

    private final DockerClient dockerClient;

    public String dockerExecute(Path directory, String image_name){


        CreateContainerResponse container =
                dockerClient.createContainerCmd(image_name)
                        .withCmd("/bin/bash", "run.sh")
                        .withWorkingDir("/workspace")
                        .withHostConfig(
                                HostConfig.newHostConfig()
                                        .withBinds(new Bind(directory.toString(), new Volume("/workspace")))
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


