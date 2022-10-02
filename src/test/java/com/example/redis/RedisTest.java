package com.example.redis;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith({SpringExtension.class})
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void redisGetStringTest() {
        Object result = redisTemplate.opsForValue().get("test");

        Assertions.assertThat(result).isEqualTo(null);
    }

    @Test
    void redisSetStringTest() {
        String userId = "userId";
        redisTemplate.opsForValue().set(userId, "1");
        Object result = redisTemplate.opsForValue().get(userId);

        Assertions.assertThat(result).isNotNull();
    }
}
