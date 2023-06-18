package com.example.redis.config;

import static com.example.redis.RedisCacheNameConst.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

  @Value("${spring.redis.host}")
  private String redisHost;

  @Value("${spring.redis.port}")
  private int redisPort;

  private final ObjectMapper redisObjectMapper;
  private final ObjectMapper cacheObjectMapper;

  public RedisConfig() {
    this.redisObjectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
        .allowIfSubType(Object.class)
        .build();

    cacheObjectMapper = redisObjectMapper.copy();
    cacheObjectMapper.activateDefaultTyping(typeValidator, DefaultTyping.NON_FINAL); //객체 저장시 class 정보도 같이 저장
  }

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort);
    return new LettuceConnectionFactory(redisStandaloneConfiguration);
  }

  @Bean
  public RedisTemplate<String, String> redisTemplate() {
    RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new StringRedisSerializer());
    redisTemplate.setConnectionFactory(redisConnectionFactory());
    return redisTemplate;
  }

  @Bean
  public RedisTemplate<String, Object> defaultRedisTemplate() {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper));
    redisTemplate.setConnectionFactory(redisConnectionFactory());
    return redisTemplate;
  }

  @Bean
  public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheManager cacheManager = RedisCacheManagerBuilder.fromConnectionFactory(connectionFactory)
        .withInitialCacheConfigurations(initialCacheConfiguration())   //캐시 이름 별 캐시 설정 세분화
        .build();
    return cacheManager;
  }

  public Map<String, RedisCacheConfiguration> initialCacheConfiguration() {
    HashMap<String, RedisCacheConfiguration> configurationMap = new HashMap<>();
    configurationMap.put(MEMBER_CACHE, resolveConfiguration(Duration.ofSeconds(30)));
    return configurationMap;
  }

  private RedisCacheConfiguration resolveConfiguration(Duration duration) {
    return RedisCacheConfiguration.defaultCacheConfig().entryTtl(duration)
        .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(cacheObjectMapper)));
  }
}
