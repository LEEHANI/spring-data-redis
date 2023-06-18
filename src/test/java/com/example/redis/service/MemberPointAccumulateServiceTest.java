package com.example.redis.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MemberPointAccumulateServiceTest {

  @Autowired
  private MemberPointService pointService;
  @Autowired
  private MemberPointAccumulateService memberPointAccumulateService;

  @Test
  public void addPointRaceConditionTest() throws InterruptedException {
    ExecutorService threadPool = Executors.newFixedThreadPool(100);
    IntStream.range(0, 100).forEach(i -> threadPool.execute(() -> pointService.accumulate(1L, 100)));
    threadPool.shutdown();
    threadPool.awaitTermination(5L, TimeUnit.SECONDS);
  }

  @Test
  public void addPointTestWithRedisLock() throws InterruptedException {
    ExecutorService threadPool = Executors.newFixedThreadPool(100);
    IntStream.range(0, 100).forEach(i -> threadPool.execute(() -> memberPointAccumulateService.accumulateWithLock(1L, 100)));
    threadPool.shutdown();
    threadPool.awaitTermination(10L, TimeUnit.SECONDS);
  }
}