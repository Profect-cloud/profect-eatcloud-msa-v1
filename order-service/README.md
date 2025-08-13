# Order Service

EatCloud MSA v1의 주문 관리 마이크로서비스입니다.

## 주요 기능

- 장바구니 관리 (Redis 캐싱)
- 주문 생성 및 관리
- 주문 상태 관리
- 서비스 간 통신 (Feign Client)
- 회로 차단기 패턴 (Circuit Breaker)

## 기술 스택

- Java 21
- Spring Boot 3.5.3
- Spring Cloud 2025.0.0 (Eureka, OpenFeign)
- PostgreSQL 17
- Redis 7
- JPA/Hibernate

## 서비스 의존성

이 서비스는 다음 외부 서비스들과 통신합니다:

| 서비스 | 포트 | 용도 |
|--------|------|------|
| **auth-service** | 8081 | 사용자 인증 및 권한 검증 |
| **customer-service** | 8082 | 고객 정보 조회 |
| **store-service** | 8085 | 가게 및 메뉴 정보 조회 |
| **payment-service** | 8087 | 결제 처리 |

## API 엔드포인트

### 기본 정보
- `GET /orders/health` - 서비스 상태 확인
- `GET /orders` - 서비스 정보 및 사용 가능한 엔드포인트
- `GET /orders/me` - API Gateway에서 전달받은 사용자 정보 확인
- `GET /orders/info` - 서비스 상세 정보
- `GET /orders/status` - 외부 서비스 연결 상태 확인

### 주문 처리
- `POST /orders/process?customerId=123&storeId=456` - 간단한 주문 처리 테스트

## 환경 설정

### application.properties (로컬 개발)
```properties
server.port=8080
spring.application.name=order-service

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/order_db
spring.datasource.username=eatcloud_user
spring.datasource.password=devpassword123

# External Services
service.auth.url=http://localhost:8081
service.customer.url=http://localhost:8082
service.store.url=http://localhost:8085
service.payment.url=http://localhost:8087
```

### application-docker.properties (컨테이너 환경)
환경 변수를 통해 설정이 주입됩니다.

## 실행 방법

### 로컬 개발 환경
```bash
./gradlew :order-service:bootRun
```

### Docker 환경
```bash
# 전체 스택 실행
./run-dev.sh

# 또는 개별 실행
docker-compose up order-service
```

### 접속 URL
- **로컬**: http://localhost:8080/orders
- **Docker**: http://localhost:8086/orders

## 회로 차단기 (Circuit Breaker)

외부 서비스 호출 실패 시 Fallback 메커니즘이 작동합니다:

- **AuthServiceFallback**: 인증 실패 시 기본적으로 허용
- **CustomerServiceFallback**: 고객 정보 조회 실패 시 기본 정보 반환
- **StoreServiceFallback**: 가게 정보 조회 실패 시 기본 정보 반환

## 개발 가이드

### 새로운 기능 추가
1. `src/main/java/com/eatcloud/orderservice/` 하위에 패키지별로 구현
2. Controller → Service → Repository 순서로 개발
3. Feign Client를 통한 외부 서비스 통신
4. Redis를 활용한 캐싱 적용

### 테스트
```bash
# 단위 테스트
./gradlew :order-service:test

# Health Check
curl http://localhost:8086/orders/health

# 서비스 상태 확인
curl http://localhost:8086/orders/status
```

## 모니터링

- **Health Check**: `/orders/health`
- **Actuator**: `/actuator/health`, `/actuator/info`
- **Eureka Dashboard**: http://localhost:8761

## 주의사항

1. **JWT 인증**: API Gateway에서 처리되므로 헤더로 사용자 정보 전달받음
2. **서비스 디스커버리**: Eureka를 통한 서비스 간 통신
3. **분산 트랜잭션**: 현재는 단순 호출, 추후 Saga 패턴 적용 예정
4. **데이터베이스**: 단일 RDS 인스턴스의 `order_db` 스키마 사용
5. **환경 분리**: 개발/프로덕션 환경별 설정 파일 관리

## 다음 개발 계획

- [ ] 실제 주문 엔티티 및 비즈니스 로직 구현
- [ ] 장바구니 Redis 캐싱 구현
- [ ] 주문 상태 변경 이벤트 처리
- [ ] 리뷰 시스템 구현
- [ ] 분산 트랜잭션 처리 (Saga Pattern)
- [ ] 메시지 큐를 통한 비동기 처리
