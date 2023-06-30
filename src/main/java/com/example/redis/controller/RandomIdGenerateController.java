package com.example.redis.controller;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class RandomIdGenerateController {

  @GetMapping("/v1/random-id")
  public ResponseEntity<String> generateId() {
    String randomId = UUID.randomUUID().toString();
    log.info("[UUID] : {}", randomId);
    return ResponseEntity.ok().body(randomId);
  }
}
