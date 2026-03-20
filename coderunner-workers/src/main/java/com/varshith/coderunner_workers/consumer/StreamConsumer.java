package com.varshith.coderunner_workers.consumer;

import com.varshith.coderunner_workers.dispatcher.SubmissionDispatcher;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class StreamConsumer {

    @Value("${spring.stream.consumer_name}")
    private String GROUP;
    private String WORKER;
    @Value("${spring.stream.stream_name}")
    private String STREAM;

    @Value("${spring.execution.maximum-thread-count:5}")
    private int threadCount;

    private final RedisTemplate<String, String> redisTemplate;
    private final SubmissionDispatcher submissionDispatcher;

    private ExecutorService executor;
    private Semaphore availableWorkers;
    private volatile boolean isRunning = true;

    @PostConstruct
    public void init() {
        log.info("StreamConsumer Starting up with {} worker threads", threadCount);
        WORKER = "worker-" + UUID.randomUUID();


        availableWorkers = new Semaphore(threadCount);


        executor = new ThreadPoolExecutor(
                threadCount, threadCount,
                0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>()
        );

        createConsumerGroup();

        Thread readerThread = new Thread(this::startReading);
        readerThread.setName("redis-stream-reader");
        readerThread.start();
    }

    public void createConsumerGroup() {
        try {
            redisTemplate.opsForStream().createGroup(STREAM, GROUP);
        } catch (Exception e) {
            log.info("Consumer group {} already exists", GROUP);
        }
    }

    private void startReading() {
        while (isRunning) {
            try {

                availableWorkers.acquire();

                List<MapRecord<String, Object, Object>> messages =
                        redisTemplate.opsForStream().read(
                                Consumer.from(GROUP, WORKER),
                                StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                                StreamOffset.create(STREAM, ReadOffset.lastConsumed())
                        );


                if (messages != null && !messages.isEmpty()) {
                    for (MapRecord<String, Object, Object> message : messages) {
                        String submissionId = (String) message.getValue().get("submissionId");
                        executor.submit(() -> processSubmission(message, submissionId));
                    }
                } else {

                    availableWorkers.release();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Network/Redis error while reading stream", e);

                availableWorkers.release();
                try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
    }

    private void processSubmission(MapRecord<String, Object, Object> message, String submissionId) {
        try {

            long startTimeMs = System.currentTimeMillis();


            boolean result = submissionDispatcher.dispatch(submissionId);

            long endTimeMs = System.currentTimeMillis();
            long totalTimeTaken = endTimeMs - startTimeMs;

            log.info("Total Worker Processing Time: {} ms", totalTimeTaken);


            if (result) {
                redisTemplate.opsForStream().acknowledge(STREAM, GROUP, message.getId());
                log.info("Successfully processed and ACKed: {}", submissionId);
            }
        } catch (Exception e) {
            log.error("Execution failed for submission: {}", submissionId, e);
        } finally {
            availableWorkers.release();
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down worker node...");
        isRunning = false;
        executor.shutdown();
    }
}