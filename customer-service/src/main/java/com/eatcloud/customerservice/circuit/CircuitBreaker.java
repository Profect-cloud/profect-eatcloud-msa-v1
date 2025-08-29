package com.eatcloud.customerservice.circuit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class CircuitBreaker {

    public enum State {
        CLOSED,    // 정상 상태 - 요청 허용
        OPEN,      // 차단 상태 - 요청 거부
        HALF_OPEN  // 반열림 상태 - 제한된 요청 허용
    }

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicReference<LocalDateTime> lastFailureTime = new AtomicReference<>();

    // 설정값
    private final int failureThreshold;        // 실패 임계값
    private final int successThreshold;        // 성공 임계값 (HALF_OPEN에서 CLOSED로 전환)
    private final long timeoutDuration;        // OPEN 상태 유지 시간 (밀리초)

    public CircuitBreaker() {
        this(5, 3, 60000); // 기본값: 실패 5회, 성공 3회, 60초 타임아웃
    }

    public CircuitBreaker(int failureThreshold, int successThreshold, long timeoutDuration) {
        this.failureThreshold = failureThreshold;
        this.successThreshold = successThreshold;
        this.timeoutDuration = timeoutDuration;
    }

    /**
     * 요청 실행 전 Circuit Breaker 상태 확인
     */
    public boolean canExecute() {
        State currentState = state.get();
        
        switch (currentState) {
            case CLOSED:
                return true;
            case OPEN:
                if (shouldAttemptReset()) {
                    log.info("Circuit Breaker 상태를 HALF_OPEN으로 변경");
                    state.set(State.HALF_OPEN);
                    return true;
                }
                log.warn("Circuit Breaker가 OPEN 상태입니다. 요청이 차단됩니다.");
                return false;
            case HALF_OPEN:
                return true;
            default:
                return false;
        }
    }

    /**
     * 성공 시 호출
     */
    public void onSuccess() {
        State currentState = state.get();
        
        if (currentState == State.HALF_OPEN) {
            int success = successCount.incrementAndGet();
            log.debug("HALF_OPEN 상태에서 성공 횟수: {}/{}", success, successThreshold);
            
            if (success >= successThreshold) {
                log.info("Circuit Breaker 상태를 CLOSED로 변경");
                state.set(State.CLOSED);
                resetCounters();
            }
        }
    }

    /**
     * 실패 시 호출
     */
    public void onFailure(Exception exception) {
        State currentState = state.get();
        
        if (currentState == State.CLOSED) {
            int failure = failureCount.incrementAndGet();
            lastFailureTime.set(LocalDateTime.now());
            log.warn("CLOSED 상태에서 실패 횟수: {}/{}", failure, failureThreshold);
            
            if (failure >= failureThreshold) {
                log.error("Circuit Breaker 상태를 OPEN으로 변경. 실패 임계값 도달");
                state.set(State.OPEN);
            }
        } else if (currentState == State.HALF_OPEN) {
            log.error("HALF_OPEN 상태에서 실패 발생. Circuit Breaker를 OPEN으로 변경");
            state.set(State.OPEN);
            resetCounters();
        }
    }

    /**
     * 타임아웃 후 HALF_OPEN으로 전환 시도
     */
    private boolean shouldAttemptReset() {
        LocalDateTime lastFailure = lastFailureTime.get();
        if (lastFailure == null) {
            return false;
        }
        
        long elapsed = java.time.Duration.between(lastFailure, LocalDateTime.now()).toMillis();
        return elapsed >= timeoutDuration;
    }

    /**
     * 카운터 초기화
     */
    private void resetCounters() {
        failureCount.set(0);
        successCount.set(0);
        lastFailureTime.set(null);
    }

    /**
     * 현재 상태 반환
     */
    public State getCurrentState() {
        return state.get();
    }

    /**
     * 상태 정보 로깅
     */
    public void logStatus() {
        State currentState = state.get();
        log.info("Circuit Breaker 상태: {}, 실패 횟수: {}, 성공 횟수: {}", 
                currentState, failureCount.get(), successCount.get());
    }

    /**
     * 수동으로 상태 리셋
     */
    public void reset() {
        log.info("Circuit Breaker를 수동으로 리셋합니다.");
        state.set(State.CLOSED);
        resetCounters();
    }
}
