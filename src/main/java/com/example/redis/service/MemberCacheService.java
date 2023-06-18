package com.example.redis.service;

import static com.example.redis.RedisCacheNameConst.MEMBER_CACHE;

import com.example.redis.entity.Member;
import com.example.redis.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberCacheService {

  private final MemberRepository memberRepository;

  @Cacheable(cacheManager = "redisCacheManager", key = "#seq", value = MEMBER_CACHE)
  public Member findMember(Long seq) {
    return memberRepository.findById(seq).get();
  }
}
