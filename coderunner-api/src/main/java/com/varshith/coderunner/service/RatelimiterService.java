package com.varshith.coderunner.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

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
