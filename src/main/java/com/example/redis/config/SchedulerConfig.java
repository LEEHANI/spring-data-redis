package com.example.redis.config;

import com.example.redis.property.ApiRateLimiterProperties;
import com.example.redis.property.ApiRateLimiterProperties.ApiRateLimiterProperty;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

  private final ValueOperations<String, String> redisOperation;
  private final List<ApiRateLimiterProperty> properties;

  public SchedulerConfig(RedisTemplate<String, String> redisTemplate, ApiRateLimiterProperties properties) {
    this.redisOperation = redisTemplate.opsForValue();
    this.properties = properties.getProperties();
  }

  @Override
  public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    ThreadPoolTaskScheduler threadPool = new ThreadPoolTaskScheduler();
    threadPool.setPoolSize(10);
    threadPool.initialize();

    taskRegistrar.setTaskScheduler(threadPool);

    properties.stream().forEach(property -> taskRegistrar.addFixedRateTask(() -> redisOperation.set(property.getRedisKey(), "0"), property.getRefreshPeriod().toMillis()));
  }
}
