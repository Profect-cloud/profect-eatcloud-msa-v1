package com.eatcloud.orderservice.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Stack;
import java.util.UUID;

/**
 * Saga 트랜잭션 관리 클래스
 * 분산 트랜잭션의 보상 로직을 관리
 */
@Slf4j
@Getter
public class SagaTransaction {
    private final String transactionId;
    private final Stack<CompensationStep> compensations = new Stack<>();
    private boolean completed = false;
    
    public SagaTransaction() {
        this.transactionId = UUID.randomUUID().toString();
    }
    
    public SagaTransaction(String transactionId) {
        this.transactionId = transactionId;
    }
    
    /**
     * 보상 단계 추가
     * @param name 보상 단계 이름
     * @param compensation 보상 로직
     */
    public void addCompensation(String name, Runnable compensation) {
        compensations.push(new CompensationStep(name, compensation));
        log.debug("Added compensation step '{}' to saga transaction: {}", name, transactionId);
    }
    
    /**
     * 트랜잭션 완료 처리
     */
    public void complete() {
        this.completed = true;
        log.info("Saga transaction completed successfully: {}", transactionId);
    }
    
    /**
     * 보상 트랜잭션 실행
     */
    public void compensate() {
        if (completed) {
            log.warn("Attempted to compensate completed transaction: {}", transactionId);
            return;
        }
        
        log.info("Starting compensation for saga transaction: {}", transactionId);
        int totalSteps = compensations.size();
        int currentStep = 0;
        
        while (!compensations.isEmpty()) {
            currentStep++;
            CompensationStep step = compensations.pop();
            
            try {
                log.info("Executing compensation step {}/{}: '{}' for transaction: {}", 
                        currentStep, totalSteps, step.name, transactionId);
                step.compensation.run();
                log.info("Compensation step '{}' completed successfully", step.name);
                
            } catch (Exception e) {
                log.error("Compensation step '{}' failed for transaction: {}. Error: {}", 
                         step.name, transactionId, e.getMessage(), e);
                // 보상 실패 시에도 계속 진행 (best effort)
                // 실패한 보상은 별도로 기록하여 수동 처리하도록 함
                recordFailedCompensation(step.name, e);
            }
        }
        
        log.info("Compensation completed for saga transaction: {} ({} steps processed)", 
                transactionId, totalSteps);
    }
    
    /**
     * 실패한 보상 기록 (추후 수동 처리를 위해)
     */
    private void recordFailedCompensation(String stepName, Exception e) {
        // TODO: 실패한 보상을 DB나 메시지 큐에 기록
        // 예: 관리자 대시보드에서 확인 가능하도록
        log.error("MANUAL_INTERVENTION_REQUIRED: Failed compensation step '{}' for transaction '{}': {}", 
                 stepName, transactionId, e.getMessage());
    }
    
    /**
     * 보상 단계 정보
     */
    private static class CompensationStep {
        private final String name;
        private final Runnable compensation;
        
        public CompensationStep(String name, Runnable compensation) {
            this.name = name;
            this.compensation = compensation;
        }
    }
}
