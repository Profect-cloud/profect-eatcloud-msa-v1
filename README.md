# 🍽 Eat Cloud Project
Goorm 프로펙트 클라우드 엔지니어링 과정 3기 – 2차 프로젝트  

## 📌 프로젝트 소개
**Eat Cloud**는 ‘배달의 민족’을 벤치마킹한 **주문 관리 플랫폼**입니다.  
1차 프로젝트에서 만든 모놀리식 애플리케이션을 **마이크로서비스 아키텍처**로 전환하고 그에 맞는 인프라까지 구축했습니다.

## 📆 개발 기간
- 25.08.07 ~ 25.08.24

## 👥 멤버 구성
- [강능요](https://github.com/teadmu)
- [정연주](https://github.com/racoi)
- [정민영](https://github.com/minmaker-komu)
- [문창주](https://github.com/munstate)

## 🛠 기술 스택
`Java` `Spring Boot` `Spring Security` `PostgreSQL` `PostGIS` `Redis` `QueryDSL` `Spring Cloud` `Netflix Eureka` `Rest Template``AWS` `GitAction` `ECS` `Docker` `MSK` `RDS`

## ✨ 주요 기능
- 각 도메인별로 마이크로서비스 분리
- Spring Cloud Netflix Eureka를 통한 서비스 디스커버리
- API Gateway를 통한 라우팅
- 공통 라이브러리 모듈
- passport 구조 내부 토큰 인증/인가
- Redis 활용 장바구니 데이터 저장 배치 처리
- TF-IDF 유사도 활용 사용자 검색 기반 메뉴 추천


## 🏗 아키텍처
### 디렉토리 구조
```
profect-eatcloud-msa-v1/
├── 📁 공통 모듈
│   ├── auto-response/                  # 공통 응답/예외 처리 모듈
│   └── auto-time/                      # 공통 시간 관리 모듈
│
├── 📁 인프라 서비스
│   ├── eureka-server/                  # 서비스 디스커버리
│   └── api-gateway/                    # API 게이트웨이
│
├── 📁 핵심 비즈니스 서비스
│   ├── auth-service/                   # 인증/인가 서비스
│   ├── customer-service/               # 고객 관리 서비스
│   ├── store-service/                  # 매장/메뉴 관리 서비스
│   ├── order-service/                  # 주문 관리 서비스
│   ├── payment-service/                # 결제 처리 서비스
│   ├── admin-service/                  # 관리자 서비스
│   └── manager-service/                # 매니저 서비스
│
├── 📁 데이터베이스 초기화
│   └── database-init/                  # SQL 스키마 및 데이터 초기화
│
└── 📁 배포 및 환경 설정
    ├── deploy/
    │   └──compose/                     # Docker Compose 설정
    └── docker-compose.yml              # 메인 Docker Compose
```
### 인프라 아키텍처
[git_readme_img-001](https://github.com/user-attachments/assets/6a54861e-5e0a-4362-9e86-2d3cb5fb90dd)

### CI/CD 아키텍처
[git_readme_img-002](https://github.com/user-attachments/assets/0fd5bc17-5b0a-4166-9c5b-1a1f09ef4809)
