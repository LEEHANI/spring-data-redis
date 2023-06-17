package com.example.redis;

import com.example.redis.entity.Member;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith({SpringExtension.class})
public class RedisTest {

  @Autowired
  private RedisTemplate<String, String> redisTemplate;
  @Autowired
  private RedisTemplate<String, Object> defaultRedisTemplate;

  @Test
  public void testForOpsForValue() {
    ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
    String key = "1";
    String value = valueOperations.get(key);
    Assertions.assertThat(value).isEqualTo(null);
  }

  @Test
  public void getValueForOpsForValue() throws InterruptedException {
    ValueOperations<String, String> operations = redisTemplate.opsForValue();
    operations.set("key", "value", Duration.ofSeconds(3));

    String result1 = operations.get("key");
    Assertions.assertThat(result1).isNotNull();

    Thread.sleep(3000);
    String result2 = operations.get("key");
    Assertions.assertThat(result2).isNull();
  }

  @Test
  public void testForOpsForSet() {
    redisTemplate.delete("fruits");

    SetOperations<String, String> setOperations = redisTemplate.opsForSet();
    setOperations.add("fruits", "apple", "banana", "watermelon", "melon");
    Boolean tomatoExists = setOperations.isMember("fruits", "tomato");              //O(1)
    Boolean watermelonExists = setOperations.isMember("fruits", "watermelon");

    Set<String> fruits = setOperations.members("fruits");

    Assertions.assertThat(tomatoExists).isEqualTo(false);
    Assertions.assertThat(watermelonExists).isEqualTo(true);
    Assertions.assertThat(fruits.size()).isEqualTo(4);
  }

  @Test
  public void testForOpsForList() {
    ListOperations<String, String> listOperations = redisTemplate.opsForList();
    listOperations.remove("key", 5, "value");                         //O(n)이 걸릴 수 있기 때문에 최대한 지양한다!
  }

  @Test
  public void testMember() {
    ValueOperations<String, Object> opsForValue = defaultRedisTemplate.opsForValue();
    Member member = Member.builder().age(20).name("mem").point(100).createdDate(LocalDateTime.now()).build();
    opsForValue.set("member", member);

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    Member result = objectMapper.convertValue(opsForValue.get("member"), Member.class);

    Assertions.assertThat(result).isEqualTo(member);
  }


}
