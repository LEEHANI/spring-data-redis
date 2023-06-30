package com.example.redis.property;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "api.rate-limiter")
@Component
@Getter
@Setter
public class ApiRateLimiterProperties {

  private static final String KEY_TEMPLATE = "rateLimiter:{method}:{uri}";

  private List<ApiRateLimiterProperty> properties;


  @EqualsAndHashCode
  @AllArgsConstructor
  @Getter
  @Setter
  @NoArgsConstructor
  public static class ApiRateLimiterProperty {

    private String uri;
    private HttpMethod method;
    private int maximumCallCount;
    private Duration refreshPeriod;

    public boolean isLimitedApi(String requestUri, String method) {
      return StringUtils.pathEquals(requestUri, uri) && this.method.matches(method);
    }

    public String getRedisKey() {
      return KEY_TEMPLATE.replace("{method}", method.name()).replace("{uri}", uri);
    }
  }
}
