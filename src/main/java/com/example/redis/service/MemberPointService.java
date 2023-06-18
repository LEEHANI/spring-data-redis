package com.example.redis.service;

import com.example.redis.entity.Member;
import com.example.redis.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberPointService {
  private final MemberRepository memberRepository;

  @Transactional
  public void accumulate(long seq, int point) {
    Member member = memberRepository.findById(seq).get();
    member.addPoint(point);
  }
}
