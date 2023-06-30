package com.example.redis.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedissonLockService {

  private final RedissonClient redissonClient;

  public void lock(String key, int waitTimeout, int ttl) {
    RLock lock = redissonClient.getLock(key);

    try {
      boolean lockExists = lock.tryLock(waitTimeout, ttl, TimeUnit.SECONDS);
      if (lockExists) {
        return;
      }
      throw new RuntimeException("Lock을 획득하지 못함.");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (RuntimeException e) {
      throw e;
    }
  }

  public void unlock(String key) {
    redissonClient.getLock(key).unlock();
  }
}
