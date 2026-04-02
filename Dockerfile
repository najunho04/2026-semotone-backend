# JDK 21 이미지 사용 (본인 버전에 맞게 수정)
FROM openjdk:21-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 jar 파일을 컨테이너 안으로 복사
COPY build/libs/*SNAPSHOT.jar app.jar

# 컨테이너가 켜질 때 실행할 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]