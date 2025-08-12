#!/bin/bash

# EatCloud MSA v1 - 개발 환경 실행 스크립트 (단일 RDS 버전)

echo "🚀 EatCloud MSA v1 개발 환경을 시작합니다..."

# 환경 변수 파일 로드 (주석과 빈 줄 제외)
if [ -f "deploy/env/dev/.env" ]; then
    export $(grep -v '^#' deploy/env/dev/.env | grep -v '^$' | xargs)
    echo "✅ 개발 환경 변수 로드 완료 (deploy/env/dev/.env)"
else
    echo "⚠️  deploy/env/dev/.env 파일이 없습니다. 기본 설정으로 진행합니다."
fi

# 로그 디렉토리 생성
mkdir -p logs
echo "📁 로그 디렉토리 생성 완료"

# Docker Compose 실행 (공통 + 개발 환경)
echo "🐳 Docker Compose로 서비스를 시작합니다..."
docker-compose -f deploy/compose/.yml -f deploy/compose/dev/.yml up --build

echo ""
echo "✅ 개발 환경이 시작되었습니다!"
echo "==========================================="
echo "📊 Eureka Server: http://localhost:8761"
echo "🌐 API Gateway: http://localhost:8080"
echo "👤 User Service: http://localhost:8081"
echo "🏪 Store Service: http://localhost:8082"
echo "📦 Order Service: http://localhost:8083"
echo "💳 Payment Service: http://localhost:8084"
echo "🗄️ Redis: localhost:6379"
echo "🗃️ PostgreSQL (통합): localhost:5432"
echo "   - user_db, store_db, order_db, payment_db"
echo "==========================================="
echo ""
echo "📝 데이터베이스 접속 정보:"
echo "   Host: localhost"
echo "   Port: 5432"
echo "   User: eatcloud_user"
echo "   Password: devpassword123"
echo "   Databases: user_db, store_db, order_db, payment_db"
echo ""
echo "🛠️ 데이터베이스 접속 명령어:"
echo "   docker exec -it eatcloud-db psql -U eatcloud_user -d eatcloud_db"
echo "==========================================="
