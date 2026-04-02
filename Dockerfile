# 기존의 openjdk 대신 eclipse-temurin 21 버전 사용
FROM eclipse-temurin:21-jdk-jammy

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 jar 파일을 컨테이너 안으로 복사
COPY build/libs/*SNAPSHOT.jar app.jar

# 컨테이너가 켜질 때 실행할 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]