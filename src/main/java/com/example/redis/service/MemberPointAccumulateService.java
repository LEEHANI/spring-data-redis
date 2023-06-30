package com.example.redis.service;

import static com.example.redis.RedisCacheNameConst.MEMBER_LOCK_CACHE;
import static com.example.redis.RedisCacheNameConst.resolveKey;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberPointAccumulateService {

  private final LettuceLockService lettuceLockService;
  private final RedissonLockService redissonLockService;
  private final MemberPointService memberPointService;

  public void accumulateWithLock(long memberSeq, int point) {
    String key = resolveKey(MEMBER_LOCK_CACHE, String.valueOf(memberSeq));
    try {
      lettuceLockService.spinLock(key, Duration.ofSeconds(3));
      memberPointService.accumulate(memberSeq, point);
    } catch (Exception e) {
      log.error("[MEMBER_POINT_ACCUMULATE_ERROR] : {}, {}", memberSeq, point, e);
    } finally {
      lettuceLockService.unlock(key);
    }
  }

  public void accumulateWithRetryLock(long memberSeq, int point) {
    String key = resolveKey(MEMBER_LOCK_CACHE, String.valueOf(memberSeq));
    try {
      lettuceLockService.retryLock(key, Duration.ofSeconds(3), 5);
      memberPointService.accumulate(memberSeq, point);
    } catch (Exception e) {
      log.error("[MEMBER_POINT_ACCUMULATE_ERROR] : {}, {}", memberSeq, point, e);
    } finally {
      lettuceLockService.unlock(key);
    }
  }

  public void accumulateWithPubSubLock(long memberSeq, int point) {
    String key = resolveKey(MEMBER_LOCK_CACHE, String.valueOf(memberSeq));
    try {
      redissonLockService.lock(key, 5, 3);
      memberPointService.accumulate(memberSeq, point);
    } catch (Exception e) {
      log.error("[MEMBER_POINT_ACCUMULATE_ERROR] : {}, {}", memberSeq, point, e);
    } finally {
      redissonLockService.unlock(key);
    }
  }

}
