package com.example.redis.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LettuceLockService {

  private final RedisTemplate<String, String> redisTemplate;

  public void spinLock(String key, Duration duration) {
    //얻을때까지 시도
    while (true) {
      Boolean lockExist = redisTemplate.opsForValue().setIfAbsent(key, "1", duration);
      if (lockExist) {
        break;
      }
    }
  }


  /**
   * 하나의 데이터를 굉장히 많은 유저들이 동시에 갱신할 수 있는 경우에는 사용할 수 없음.
   * waitGetLockMills = 50ms, 지연 시간은 2배씩 증가
   * 50, 100, 200, 400, 800 --> 최대 1.5초 지연이 발생할 수 있음.
   */
  public void retryLock(String key, Duration duration, int retryNumber) {
    long waitGetLockMills = 50;
    for (int i = 0; i < retryNumber; i++) {
      Boolean lockExist = redisTemplate.opsForValue().setIfAbsent(key, "1", duration);
      waitGetLock(waitGetLockMills * (i + 1)); //back-off
      waitGetLockMills = waitGetLockMills << 1;
      if (lockExist) {
        return;
      }
    }
    throw new RuntimeException("Lock을 획득하지 못함.");
  }

  private void waitGetLock(long waitGetLockMills) {
    try {
      Thread.sleep(waitGetLockMills);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public void unlock(String key) {
    redisTemplate.delete(key);
  }
}
