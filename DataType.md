
# Data Type

## Keys 설정 
- string 뿐만 아니라 JPEG 같은 모든 바이너리 시퀀스를 키로 사용할 수 있음. 
- 빈 문자열도 가능.

### key 설계 규칙 
- 너무 긴 값을 키로 두지 말자. 메모리 측면 뿐만 아니라 조회시에도 비용이 많이 듦. 길다면 해시 함수를 적용하자.
- 너무 짧은 값을 키로 두지 말자. `user:1000:followers`를 `u1000flw`로 줄이는건 그다지 효율적이지 않다.
- 스키마를 지키자. ex) "object-type:id"
- maximum key size 512MB.

### key 삭제 
- ```
  > set mykey somevalue
  OK
  > del mykey
  (interger) 1 //mykey 제거
  > del mykey
  (interger) 0 //mykey 존재안함 
  ```

### Key expiration 
- 만료 시간을 지정하지 않으면 무제한임. 
- seconds or milliseconds 설정 가능 
- expire 정보는 디스크에 복제되어 유지되며, redis 가 중지된 상태일 때는 시간이 가상으로 경과함.
- ```
  > set mykey 100 ex 10
  OK 
  > ttl mykey
  (integer) 9 //남은 시간
  ```
## Strings
- set [key] [value]
- ```
  > set mykey somevalue
  OK
  > get mykey
  "somevalue"
  ```
- `nx`는 key가 존재 하지 않을 때 저장함. set the key if it does not already exist
- ```
  > set mykey newval nx 
  OK 
  > set mykey newval nx
  (nil)  //키가 존재하면 nil로 출력
  ```
- set mykey newval xx 키가 존재하면 OK

## Lists
- `Linked List`로 구현됨. 데이터 갯수에 상관없이 맨 앞, 맨 뒤에 데이터 삽입 시간이 동일함. (양방향) 
- Redis 는 빠르게 데이터를 추가하는 게 중요하므로 링크드 리스트를 택한듯.  
- index access 가 느리다. 중간 데이터를 빠르게 찾고 싶으면 sorted sets을 사용하는게 낫다.
- `rpush`는 머리에 추가, `lpush`는 꼬리에 추가, `lrange`는 범위 요소 추출 
- ```
  > rpush mylist A
  (integer) 1
  > rpush mylist B
  (integer) 2
  > lpush mylist first
  (integer) 3
  > lrange mylist 0 -1 //0부터 끝까지 조회
  1) "first"
  2) "A"
  3) "B"
  ```
- rpush, lpush로 여러개 삽입 가능
- ```
  > rpush mylist C D E
  (integer) 6
  > lrange mylist 0 -1
  1) "first"
  2) "A"
  3) "B"
  4) "C"
  5) "D"
  6) "E" 
  ```
- pop은 value를 제거하고 조회하는 방식. 비어있으면 (nil) 출력 
- ```
  > lpop mylist 
  "E" 
  ```

## Sets

## Hashes

## Sorted Sets

## Streams
- 추가 전용 로그처럼 사용하는 데이터 구조
- 발생한 순서대로 이벤트를 기록한 다 처리를 위해 신디케이트 하는데 도움된다.

## Geospatial indexes. 지리공간 인덱스
- 지정된 지리적 반경 또는 경계 상자 내에서 위치 찾는데 유용

## Bitmaps
- 문자열에서 비트 연산을 수행할 수 있다. 

## Bitfields
- 문자열 값에서 여러 카운터를 효율적으로 인코딩함 

## HyperLogLog
- 대규모 세트의 카디널리티의 확률적 추정치를 제공 