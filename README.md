
# Spring-Data-Redis

## Redis 란?
- Key-Value 형태로 데이터를 관리
- in-memory 데이터 구조 저장소
- strings, hashes, lists, sets, sorted sets 등의 데이터 구조 제공
- caching, 분산락, queuing, event processing 문제를 해결
- High Availability 를 위해 Redis Sentinel 및 Redis Cluster를 통한 자동 파티셔닝 제공
- 비동기식(asynchronous replication) 복제를 지원한다.

## docker

#### redis 설치 및 실행
- docker pull redis:alpine
- docker run -d -p 6379:6379 redis:alpine

#### mysql 설치 및 실행
- docker pull mysql
- docker run --name mysql-container -e MYSQL_ROOT_PASSWORD=1234 -d -p 3306:3306 mysql

#### redis 접속
- docker exec -it [CONTAINER ID] sh

#### redis 접속 테스트
- `redis-cli`: 레디스한테 명령을 보낼 수 있는 레디스 명령 유틸리티
- ```
  $ redis-cli
  127.0.0.1:6379> ping
  PONG
  ```

## redis 기본 연결
```
@Bean
public RedisConnectionFactory redisConnectionFactory() {
  RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort);
  return new LettuceConnectionFactory(redisStandaloneConfiguration);
}
```
- 스프링 부트에서는 기본적으로 lettuce를 사용

### Redis RedisTemplate
```
@Bean
public RedisTemplate<String, String> redisTemplate() {
  RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
  redisTemplate.setKeySerializer(new StringRedisSerializer());
  redisTemplate.setValueSerializer(new StringRedisSerializer());
  redisTemplate.setConnectionFactory(redisConnectionFactory());
  return redisTemplate;
}
```
- spring data redis에서 redis에 operation 위해 사용하는 방식
- key, value 데이터 타입에 따라 여러개를 선언하여 사용함.
  + RedisTemplate<String, String> redisTemplate, RedisTemplate<String, Object> defaultRedisTemplate
- key, value 간 데이터를 serialize, deserialize 설정이 필요함. ex) `new StringRedisSerializer()`
```
@Bean
public RedisTemplate<String, Object> defaultRedisTemplate() {
  RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
  redisTemplate.setKeySerializer(new StringRedisSerializer());
  redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(redisObjectMapper));
  redisTemplate.setConnectionFactory(redisConnectionFactory());
  return redisTemplate;
}
```
- redis에 객체 타입을 저장하면 내부적으로 클래스 정보를 갖고 있음.
- `["com.example.redis.entity.Member",{"seq":1,"age":20,"name":"홍길동","point":10000}]`
- 직렬화, 역직렬화 할때 객체 정보를 참조함. 해당 경로의 객체를 갖고있지 않으면 역직렬화가 불가능

#### LocalDateTime
- LocalDateTime을 직렬화, 역직렬화 하는 작업이 까다로움. jackson-datatype-jsr310 의존성이 필요하고, objectMapper를 섬세하게 다룰 수 있어야 함.
```
this.redisObjectMapper = new ObjectMapper()
  .registerModule(new JavaTimeModule())
  .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
```
- 위 설정을 objectmapper 에 추가해줘야 localDateTime 이 String 형태로 저장됌. `"createdDate":"2023-06-17T15:17:06"`
- 그러나 redis가 내부적으로 참조하던 클래스 정보를 저장하지 않기 때문에, 객체 데이터를 가져올때 objectmapper 로 명시적으로 변환을 해줘야함.
```
ObjectMapper objectMapper = new ObjectMapper()
  .registerModule(new JavaTimeModule())
  .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
Member result = objectMapper.convertValue(opsForValue.get("member"), Member.class);
```

## Redis Cache 사용

### cache 설정
- @EnableCaching
```
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
```
- 캐시 이름별로 캐시 설정을 세분화 할 수 있다.
- 여기선 MEMBER_CACHE로 만들었고, ttl은 30초로 설정함. objectma per 에는 class 정보도 같이 저장하도록 설정함.


### 캐시 사용
```
@Cacheable(cacheManager = "redisCacheManager", key = "#seq", value = MEMBER_CACHE)
public Member findMember(Long seq) {
  return memberRepository.findById(seq).get();
}
```
- key는 redis에 저장될 키 정보이다. 키를 생성할 때 파라미터 값으로만 생성하면 충돌이 발생할 수 있다. 그래서 내부적으로 value값을 조합해서 같이 생성한다. (메서드명으로 x) 여기서는 `MEMBER_CACHE::seq` 형식으로 저장된다. ex) `member::1`
- value에는 캐시 설정 이름을 넣어준다.
- 물론, redisTemplate으로도 캐시 용도로 사용 가능하지만, @Cacheable 어노테이션으로 캐시를 사용하는게 더 간편해서 많이 쓰이는 방식이다.
- 주의해야 할 점은, @Cacheable 어노테이션을 사용하면 객체 파싱할 때 class 정보를 넣어줘야 한다..
- redisTemplate을 사용하면 데이터를 꺼내오고, objectmapper로 변환해주면 되지만, @Cacheable은 메서드 자체를 캐시로 사용하는 것이기 때문에 추가적으로 변환하는 코드를 넣기 어렵다.

## Redis 분산락
- 멀티 쓰레드 환경에서 동시성 이슈가 발생한다.

### 동시성 이슈
```
@Transactional
public void accumulate(long seq, int point) {
  Member member = memberRepository.findById(seq).get();
  member.addPoint(point);
}

@Test
public void addPointRaceConditionTest() throws InterruptedException {
  ExecutorService threadPool = Executors.newFixedThreadPool(100);
  IntStream.range(0, 100).forEach(i -> threadPool.execute(() -> pointService.accumulate(1L, 100)));
  threadPool.shutdown();
  threadPool.awaitTermination(5L, TimeUnit.SECONDS);
}
```
- 쓰레드 100개를 만들고, 멤버1의 포인트를 100 증가시켜보자. 그 결과 멤버1의 포인트가 10000점일 것 같지만, 동시성 이슈로 인해 10000점이 아닐 가능성이 매우 높다.

### redis lock
- 레디스를 이용해, 락을 걸어 동시성 이슈를 해결해보자.
- 레디스에 값을 넣어서 락을 획득하고, 작업이 끝나면 키를 삭제하여 언락을 하면 된다.
```
public void accumulateWithLock(long memberSeq, int point) {
  String key = resolveKey(MEMBER_LOCK_CACHE, String.valueOf(memberSeq));
  memberLockService.lock(key, Duration.ofSeconds(3));
  memberPointService.accumulate(memberSeq, point);
  memberLockService.unlock(key);
}
```
- 여기서 lock을 얻을 때까지 시도하는 스핀 락 형태로 구현되어있다.
- 구현시 주의해야할 점
  + key가 겹치지 않게 키 설계에 주의해야 한다.
  + lock을 얻는 작업을 제일 먼저 진행해야 한다.
  + lock을 얻는 작업은 트랜잭션 범위 밖에서 해야한다. (lock을 얻을 때까지 커넥션을 갖고있으면, 커넥션 부족 이슈 발생)
  + `setIfAbsent`로 atomic하게 동작해야 한다.
  + redis 서버가 죽으면 락 해제가 안될 수 있으므로 ttl 설정을 필수로 해줘야 한다.


## Redis 보안
- 기본 인증 절차가 없음.
- 보안이 필요하면 6379 포트에 방화벽이 필요할듯
- requirepass 옵션을 사용하여 auth 명령어 인증으로 보안 계층 추가해야할듯

## redis persistence
- redis 는 디스크에 저장(백업)을 위해 여러 옵션을 제공한다.
- RDS(Redis Database)
- AOF(Append Only File)
- No persistence
- RDB + AOF

#### RDB(Redis Database)
- 지정된 간격으로 스냅샷을 뜬다.
- 예를들어 최소 5분 및 100회 쓰기 시 스냅샷 저장하도록 설정할 수 있다.

##### 장점
- 컴팩트하게 하나의 파일로 보관할 수 있다.
- redis 가 다운됐을 시, 쉽게 복원할 수 있다.
- AOF 보다 빠르게 복원할 수 있다.

##### 단점
- redis 가 멈췄을 때, 데이터 손실을 최소화 해야하는 경우 좋지 않다.
- 일반적으로 5분 이상마다 RDB 스냅샷을 생성하므로, 스냅샷 생성 전에 멈추면 최신 데이터 손실이 발생할 수 있다.
- fork()로 자식 프로세스를 만들어서 디스크 저장 작업을 한다. 이 fork() 작업은 데이터가 큰 경우 시간이 오래걸릴 수 있으며, redis 클라이언트 작업이 중단될 수 있다.

#### AOF(Append Only File)
- redis write/update 작업을 모두 log에 기록해두고, 서버 재 시작 시 log 파일을 읽어서 데이터를 복구한다.

##### 장점
- 훨씬 더 내구성있음.
- 백그라운드 스레드를 활용하여 기록함. 메인 스레드가 진행중인 작업이 없을 때 수행.
- AOF 로그는 append만 하기 때문에 write 속도가 빠르고, 정전이 발생해도 손상되지 않는다.
- text 파일로 저장되므로 수정이 가능하다.

##### 단점
- RDB 파일보다 크다.

##### AOF rewrite
- 특정 시점에 데이터 전체를 다시 쓰는 기능이 있음.
- 파일이 너무 크면 OS 파일 사이즈 제한에 걸리거나, Redis 재시작시 AOF 파일 로드하는데 시간이 오래걸림
- 클라이언트 서비스 중단하지 않고 백그라운드에 AOF 재구축

### Redis Client
#### Redisson
#### Jedis
#### lettuce



##### ref
- https://redis.io/docs/