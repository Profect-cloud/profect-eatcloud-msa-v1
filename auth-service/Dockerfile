FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 멀티모듈 빌드 결과 복사
COPY auth-service/build/libs/auth-service-*.jar app.jar

EXPOSE 8081

# 환경변수로 프로파일 설정
ENTRYPOINT ["java", "-jar", "app.jar"]