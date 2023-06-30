package com.example.redis.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@SpringBootTest
@AutoConfigureMockMvc
class RandomIdGenerateControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void generateRandomId() throws Exception {
    ExecutorService threadPool = Executors.newFixedThreadPool(100);
    IntStream.range(0, 1).forEach(i -> threadPool.execute(() -> {
      try {
        mockMvc.perform(get("/v1/random-id")).andDo(MockMvcResultHandlers.print());
      } catch (Exception e) {
      }
    }));

    threadPool.shutdown();
    threadPool.awaitTermination(5, TimeUnit.MINUTES);
  }
}