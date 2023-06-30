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

  @Test
  public void addPointTestWithRetryLock() throws InterruptedException {
    ExecutorService threadPool = Executors.newFixedThreadPool(100);
    IntStream.range(0, 100).forEach(i -> threadPool.execute(() -> memberPointAccumulateService.accumulateWithRetryLock(1L, 100)));
    threadPool.shutdown();
    threadPool.awaitTermination(10L, TimeUnit.SECONDS);

    //100개가 동시에 들어오는 경우에는 당연히 제대로 동작하지 않음.
    // 하지만 실무 환경에서 이런 상황이 나올 수 없다면, 충분히 사용할 수 있다.
  }

  @Test
  public void addPointTestWithPubSubLock() throws InterruptedException {
    ExecutorService threadPool = Executors.newFixedThreadPool(100);
    IntStream.range(0, 100).forEach(i -> threadPool.execute(() -> memberPointAccumulateService.accumulateWithPubSubLock(1L, 100)));
    threadPool.shutdown();
    threadPool.awaitTermination(10L, TimeUnit.SECONDS);
  }
}