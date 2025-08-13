FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 멀티모듈 빌드 결과 복사
COPY store-service/build/libs/store-service-*.jar app.jar

EXPOSE 8085

# 환경변수로 프로파일 설정
ENTRYPOINT ["java", "-jar", "app.jar"]