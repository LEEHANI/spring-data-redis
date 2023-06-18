package com.example.redis.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Member {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long seq;
  @Column
  private Integer age;
  @Column
  private String name;
  @Column(nullable = false)
  private Integer point;
  @Column(nullable = false)
  private LocalDateTime createdDate;

  public void addPoint(int point) {
    this.point = this.point + point;
  }
}
