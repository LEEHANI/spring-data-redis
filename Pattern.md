
# Redis programming pattern 

### 분산 락

#### 분산 락 결함. 
- 서버1이 lock1을 갖고있는데, master redis가 죽었다. 죽어서 데이터 복제를 하지 못한채로 다른 replica 가 master로 바뀌어 버린다면, lock1을 서버1이 아닌 다른 서버가 갖게될 수 있다. 
- 하지만 이는 미미한 시간이니 거의 발생하지 않는다.. 
- 이를 해결하기 위해 Redlock 알고리즘이 발명됐고, redisson에서 쓸 수 있다. 