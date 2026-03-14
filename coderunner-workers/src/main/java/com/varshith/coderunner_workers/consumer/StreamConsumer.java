package com.varshith.coderunner_workers.consumer;


import com.varshith.coderunner_workers.dispatcher.SubmissionDispatcher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class StreamConsumer {

    @Value("${spring.stream.consumer_name}")
    private String GROUP;
    private String WORKER;
    @Value("${spring.stream.stream_name}")
    private String STREAM;


    private final RedisTemplate<String, String> redisTemplate;
    private final SubmissionDispatcher submissionDispatcher;

    public void createConsumerGroup(){
        try{
            log.info("Creating consumer group");
            redisTemplate.opsForStream().createGroup(STREAM, GROUP);
        } catch (Exception e){
            log.warn("Consumer group might already exist or creation failed");
        }

    }

    @PostConstruct
    public void init(){
        log.info("StreamConsumer Starting up");
        WORKER="worker-"+ UUID.randomUUID();
        createConsumerGroup();
        Thread thread = new Thread(this::startReading);
        thread.start();
    }

    private void startReading(){

        while(true){
            List<MapRecord<String, Object, Object>> messages =
                    redisTemplate.opsForStream().read(
                            Consumer.from(GROUP, WORKER),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(STREAM, ReadOffset.lastConsumed())
                    );

            if (messages != null && !messages.isEmpty()) {
                for (MapRecord<String, Object, Object> message : messages) {
                    log.info("Received message: {}", message);

                    String submissionId =
                            (String) message.getValue().get("submissionId");
                    log.info("Submission processing: {}", submissionId);
                    // execute submission here
                    submissionDispatcher.dispatch(submissionId);

                    redisTemplate.opsForStream().acknowledge(
                            STREAM,
                            GROUP,
                            message.getId()
                    );
                    log.info("Acknowledged acknowledgement: {}", submissionId);
                }
            }
        }

    }


}
