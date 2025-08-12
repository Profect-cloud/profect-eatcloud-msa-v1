#!/bin/bash

# EatCloud MSA v1 - 프로덕션 환경 실행 스크립트 (단일 RDS 버전)

echo "🚀 EatCloud MSA v1 프로덕션 환경을 시작합니다..."

# AWS RDS 연결 확인
echo "🔍 AWS RDS 연결 확인..."
if [ -z "$DB_HOST" ]; then
    echo "❌ 오류: DB_HOST 환경 변수가 설정되지 않았습니다."
    echo "💡 deploy/env/prod/ 디렉토리의 환경 변수 파일들을 확인하세요."
    exit 1
fi

# 보안 확인
if [ ! -f "deploy/env/prod/.env.local" ]; then
    echo "⚠️  경고: deploy/env/prod/.env.local 파일이 없습니다."
    echo "🔐 프로덕션 환경에서는 보안을 위해 실제 환경 변수를 별도 파일로 관리하세요."
fi

# 환경 변수 파일 로드 (주석과 빈 줄 제외)
if [ -f "deploy/env/prod/.env" ]; then
    export $(grep -v '^#' deploy/env/prod/.env | grep -v '^$' | xargs)
    echo "✅ 프로덕션 환경 변수 로드 완료 (deploy/env/prod/.env)"
fi

if [ -f "deploy/env/prod/.env.local" ]; then
    export $(grep -v '^#' deploy/env/prod/.env.local | grep -v '^$' | xargs)
    echo "✅ 로컬 프로덕션 환경 변수 로드 완료 (deploy/env/prod/.env.local)"
fi

# RDS 연결 테스트
echo "🔗 AWS RDS 연결 테스트..."
if command -v pg_isready &> /dev/null; then
    pg_isready -h "$DB_HOST" -p "${DB_PORT:-5432}" -U "$DB_USER"
    if [ $? -eq 0 ]; then
        echo "✅ AWS RDS 연결 성공"
    else
        echo "❌ AWS RDS 연결 실패. 연결 정보를 확인하세요."
        echo "   Host: $DB_HOST"
        echo "   Port: ${DB_PORT:-5432}"
        echo "   User: $DB_USER"
        exit 1
    fi
else
    echo "⚠️  pg_isready가 설치되지 않아 연결 테스트를 건너뜁니다."
fi

# 로그 디렉토리 생성
mkdir -p /app/logs
echo "📁 로그 디렉토리 생성 완료"

# Docker Compose 실행 (공통 + 프로덕션 환경)
echo "🐳 Docker Compose로 프로덕션 서비스를 시작합니다..."
docker-compose -f deploy/compose/.yml -f deploy/compose/prod/.yml up -d --build

echo ""
echo "✅ 프로덕션 환경이 시작되었습니다!"
echo "==========================================="
echo "🌐 API Gateway: http://localhost (포트 80)"
echo "📊 Eureka Server는 내부 네트워크에서만 접근 가능"
echo "🗃️ AWS RDS: $DB_HOST:${DB_PORT:-5432}"
echo ""
echo "🔍 유용한 명령어:"
echo "   상태 확인: docker-compose -f deploy/compose/.yml -f deploy/compose/prod/.yml ps"
echo "   로그 확인: docker-compose -f deploy/compose/.yml -f deploy/compose/prod/.yml logs -f [서비스명]"
echo "   중지: docker-compose -f deploy/compose/.yml -f deploy/compose/prod/.yml down"
echo ""
echo "🔧 RDS 관리:"
echo "   모니터링: AWS RDS 콘솔에서 확인"
echo "   백업: 자동 백업 설정 확인"
echo "   보안: VPC 보안 그룹 설정 확인"
echo "==========================================="
