package com.example.redis.service;

import com.example.redis.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MemberCacheServiceTest {

  @Autowired
  private MemberCacheService memberCacheService;

  @Test
  public void testForMemberCache() {
    Member member = memberCacheService.findMember(1L);
    Member member2 = memberCacheService.findMember(1L);
  }

}