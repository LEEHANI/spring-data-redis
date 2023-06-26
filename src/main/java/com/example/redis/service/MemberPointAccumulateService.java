package com.example.redis.service;

import static com.example.redis.RedisCacheNameConst.MEMBER_LOCK_CACHE;
import static com.example.redis.RedisCacheNameConst.resolveKey;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberPointAccumulateService {

  private final LettuceLockService lettuceLockService;
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

}
