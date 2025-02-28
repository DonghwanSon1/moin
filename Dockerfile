# 1. 기본 이미지 선택
FROM openjdk:17-jdk

# 2. 작업 디렉토리 설정
WORKDIR /spring-boot

# 3. 이미 빌드된 JAR 파일 복사
COPY build/libs/*SNAPSHOT.jar app.jar

# 4. 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/spring-boot/app.jar"]