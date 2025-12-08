# ===================================
# Stage 1: Build Stage (Gradle 빌드)
# ===================================

# 1. 베이스 이미지 가져오기
FROM gradle:8.5-jdk17 AS build

# 캐시 무효화를 위한 빌드 인자 (매번 다른 값 전달)
ARG BUILD_DATE
ARG GIT_COMMIT
RUN echo "Build Date: $BUILD_DATE, Commit: $GIT_COMMIT"

# 2. 작업 폴더 만들기
# 컨테이너 안에 /app 이라는 폴더를 만들고 거기로 이동합니다.
WORKDIR /app

# 3. 설정 파일들 먼저 복사 (★중요: 캐싱 전략)
# 소스코드(src)보다 설정 파일을 먼저 복사하는 이유:
# 소스코드를 고쳐도 라이브러리 의존성이 안 바뀌면, 아래 4번 과정을 건너뛰고
# 기존에 다운받아둔 것을 재사용(캐시)해서 빌드 속도가 엄청 빨라집니다.
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY gradlew ./

# 4. 의존성(라이브러리) 미리 다운로드
# 소스코드가 없어도 라이브러리만 미리 받아둡니다.
# '|| true'는 혹시라도 의존성 다운로드 중 사소한 에러가 나도 멈추지 말라는 안전장치입니다.
RUN ./gradlew dependencies --no-daemon || true

# 5. 소스 코드 복사
# 이제 진짜 작성하신 코드를 복사해 넣습니다.
COPY src ./src

# 6. 실행 파일(JAR) 만들기
# Gradle로 빌드해서 실행 가능한 jar 파일을 만듭니다.
# '-x test': 테스트 코드는 돌리지 않고 빌드만 빠르게 수행합니다.
RUN ./gradlew bootJar --no-daemon -x test

# ===================================
# Stage 2: Runtime Stage (실행 환경)
# ===================================
FROM eclipse-temurin:17-jre

# 메타데이터
LABEL maintainer="your-email@example.com"
LABEL description="Kotlin Board API with Spring Boot"
LABEL version="1.0.0"

# 작업 디렉토리
WORKDIR /app

# 빌드 스테이지에서 JAR 파일 복사
COPY --from=build /app/build/libs/kotlin-board-*.jar app.jar

# 포트 노출
EXPOSE 8080

# 헬스체크 (30초마다 확인)
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
# -Dspring.profiles.active: 실행 프로파일 지정
# -Xmx512m: 최대 메모리 512MB
ENTRYPOINT ["java", \
    "-Dspring.profiles.active=prod", \
    "-Xmx512m", \
    "-XX:+UseContainerSupport", \
    "-jar", \
    "app.jar"]
