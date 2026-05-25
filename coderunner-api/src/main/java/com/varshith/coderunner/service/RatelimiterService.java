package com.varshith.coderunner.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/*
* Rate limiter class uses a simple time based rate limiter where if a user submits four times within a single second, return false and then the user request gets reverted.
* */

@Service
@RequiredArgsConstructor
public class RatelimiterService {

    private final StringRedisTemplate redisTemplate;

    public boolean checkRequest(String key) {
        String redisKey = "ratelimit:" + key;
        Boolean ok = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", Duration.ofMillis(250));
        return Boolean.TRUE.equals(ok);
    }
}
