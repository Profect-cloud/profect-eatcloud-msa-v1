package com.eatcloud.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 기반 분산락 서비스
 * 분산 환경에서 동시성 제어 및 트랜잭션 보장
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DistributedLockService {
    
    private final RedissonClient redissonClient;
    private static final String LOCK_PREFIX = "lock:";
    
    /**
     * 분산락 획득 및 작업 실행
     * @param key 락 키
     * @param waitTime 락 획득 대기 시간
     * @param leaseTime 락 유지 시간
     * @param unit 시간 단위
     * @param task 실행할 작업
     * @return 작업 결과
     */
    public <T> T executeWithLock(String key, long waitTime, long leaseTime, TimeUnit unit, 
                                  Callable<T> task) throws Exception {
        RLock lock = redissonClient.getFairLock(LOCK_PREFIX + key);
        boolean acquired = false;
        
        try {
            acquired = lock.tryLock(waitTime, leaseTime, unit);
            
            if (!acquired) {
                log.warn("Failed to acquire lock for key: {}, thread: {}", key, Thread.currentThread().getId());
                throw new RuntimeException("Failed to acquire lock for key: " + key);
            }
            
            log.debug("Lock acquired: key={}, thread={}", key, Thread.currentThread().getId());
            return task.call();
            
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Lock released: key={}, thread={}", key, Thread.currentThread().getId());
            }
        }
    }
    
    /**
     * 분산락 획득 시도 (논블로킹)
     * @param key 락 키
     * @param leaseTime 락 유지 시간
     * @param unit 시간 단위
     * @return 락 획득 성공 여부
     */
    public boolean tryLock(String key, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getFairLock(LOCK_PREFIX + key);
        
        try {
            boolean acquired = lock.tryLock(0, leaseTime, unit);
            if (acquired) {
                log.debug("Lock acquired immediately: key={}, thread={}", key, Thread.currentThread().getId());
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock acquisition interrupted: key={}", key, e);
            return false;
        }
    }
    
    /**
     * 분산락 해제
     * @param key 락 키
     */
    public void unlock(String key) {
        RLock lock = redissonClient.getFairLock(LOCK_PREFIX + key);
        
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("Lock released: key={}, thread={}", key, Thread.currentThread().getId());
        } else {
            log.warn("Attempted to unlock non-owned lock: key={}, thread={}", key, Thread.currentThread().getId());
        }
    }
    
    /**
     * 다중 락 획득 및 작업 실행 (분산 트랜잭션용)
     * @param keys 락 키 배열
     * @param waitTime 락 획득 대기 시간
     * @param leaseTime 락 유지 시간
     * @param unit 시간 단위
     * @param task 실행할 작업
     * @return 작업 결과
     */
    public <T> T executeWithMultiLock(String[] keys, long waitTime, long leaseTime, TimeUnit unit,
                                       Callable<T> task) throws Exception {
        RLock[] locks = new RLock[keys.length];
        for (int i = 0; i < keys.length; i++) {
            locks[i] = redissonClient.getFairLock(LOCK_PREFIX + keys[i]);
        }
        
        RLock multiLock = redissonClient.getMultiLock(locks);
        boolean acquired = false;
        
        try {
            acquired = multiLock.tryLock(waitTime, leaseTime, unit);
            
            if (!acquired) {
                log.warn("Failed to acquire multi-lock for keys: {}, thread: {}", 
                        String.join(", ", keys), Thread.currentThread().getId());
                throw new RuntimeException("Failed to acquire multi-lock for keys: " + String.join(", ", keys));
            }
            
            log.debug("Multi-lock acquired: keys={}, thread={}", 
                     String.join(", ", keys), Thread.currentThread().getId());
            return task.call();
            
        } finally {
            if (acquired && multiLock.isHeldByCurrentThread()) {
                multiLock.unlock();
                log.debug("Multi-lock released: keys={}, thread={}", 
                         String.join(", ", keys), Thread.currentThread().getId());
            }
        }
    }
    
    /**
     * 읽기/쓰기 락 - 읽기 작업 실행
     * @param key 락 키
     * @param waitTime 락 획득 대기 시간
     * @param leaseTime 락 유지 시간
     * @param unit 시간 단위
     * @param task 실행할 읽기 작업
     * @return 작업 결과
     */
    public <T> T executeWithReadLock(String key, long waitTime, long leaseTime, TimeUnit unit,
                                      Callable<T> task) throws Exception {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock(LOCK_PREFIX + key);
        RLock readLock = rwLock.readLock();
        boolean acquired = false;
        
        try {
            acquired = readLock.tryLock(waitTime, leaseTime, unit);
            
            if (!acquired) {
                throw new RuntimeException("Failed to acquire read lock for key: " + key);
            }
            
            log.debug("Read lock acquired: key={}, thread={}", key, Thread.currentThread().getId());
            return task.call();
            
        } finally {
            if (acquired && readLock.isHeldByCurrentThread()) {
                readLock.unlock();
                log.debug("Read lock released: key={}, thread={}", key, Thread.currentThread().getId());
            }
        }
    }
    
    /**
     * 읽기/쓰기 락 - 쓰기 작업 실행
     * @param key 락 키
     * @param waitTime 락 획득 대기 시간
     * @param leaseTime 락 유지 시간
     * @param unit 시간 단위
     * @param task 실행할 쓰기 작업
     * @return 작업 결과
     */
    public <T> T executeWithWriteLock(String key, long waitTime, long leaseTime, TimeUnit unit,
                                       Callable<T> task) throws Exception {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock(LOCK_PREFIX + key);
        RLock writeLock = rwLock.writeLock();
        boolean acquired = false;
        
        try {
            acquired = writeLock.tryLock(waitTime, leaseTime, unit);
            
            if (!acquired) {
                throw new RuntimeException("Failed to acquire write lock for key: " + key);
            }
            
            log.debug("Write lock acquired: key={}, thread={}", key, Thread.currentThread().getId());
            return task.call();
            
        } finally {
            if (acquired && writeLock.isHeldByCurrentThread()) {
                writeLock.unlock();
                log.debug("Write lock released: key={}, thread={}", key, Thread.currentThread().getId());
            }
        }
    }
    
    /**
     * 락 상태 확인
     * @param key 락 키
     * @return 락이 걸려있는지 여부
     */
    public boolean isLocked(String key) {
        RLock lock = redissonClient.getFairLock(LOCK_PREFIX + key);
        return lock.isLocked();
    }
    
    /**
     * 현재 스레드가 락을 보유하고 있는지 확인
     * @param key 락 키
     * @return 현재 스레드가 락을 보유하고 있는지 여부
     */
    public boolean isHeldByCurrentThread(String key) {
        RLock lock = redissonClient.getFairLock(LOCK_PREFIX + key);
        return lock.isHeldByCurrentThread();
    }
}
