package com.example.redis.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisLockService {
  private final RedisTemplate<String, String> redisTemplate;

  public void lock(String key, Duration duration) {
    //얻을때까지 시도
    while (true) {
      Boolean lockExist = redisTemplate.opsForValue().setIfAbsent(key, "1", duration);
      if (lockExist) {
        break;
      }
    }
  }

  public void unlock(String key) {
    redisTemplate.delete(key);
  }
}
