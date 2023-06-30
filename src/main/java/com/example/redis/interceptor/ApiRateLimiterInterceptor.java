package com.example.redis.interceptor;

import com.example.redis.property.ApiRateLimiterProperties;
import com.example.redis.property.ApiRateLimiterProperties.ApiRateLimiterProperty;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class ApiRateLimiterInterceptor implements HandlerInterceptor {

  private final ValueOperations<String, String> redisOperation;
  private final List<ApiRateLimiterProperty> properties;

  public ApiRateLimiterInterceptor(RedisTemplate<String, String> redisTemplate, ApiRateLimiterProperties properties) {
    this.redisOperation = redisTemplate.opsForValue();
    this.properties = properties.getProperties();
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String requestURI = request.getRequestURI();
    String method = request.getMethod();

    Optional<ApiRateLimiterProperty> limiterProperty = properties.stream()
        .filter(limiter -> limiter.isLimitedApi(requestURI, method))
        .findFirst();

    if (limiterProperty.isPresent()) {
      ApiRateLimiterProperty property = limiterProperty.get();
      Long tokens = redisOperation.increment(property.getRedisKey());
      if (tokens != null && tokens <= property.getMaximumCallCount()) {
        return true;
      } else {
        log.info("[TOO_MANY_REQUESTS] key: {}", property.getRedisKey());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        return false;
      }
    }
    return true;
  }
}
