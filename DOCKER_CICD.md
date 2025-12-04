# Docker & CI/CD 구축 가이드

> Kotlin Board API를 Docker로 컨테이너화하고 GitHub Actions로 자동 배포하는 과정

---

## 📋 목차

1. [Docker 로컬 실행](#1-docker-로컬-실행)
2. [Docker Compose 구성](#2-docker-compose-구성)
3. [GitHub Actions CI 구축](#3-github-actions-ci-구축)
4. [GitHub Actions CD 구축](#4-github-actions-cd-구축)
5. [AWS EC2 배포](#5-aws-ec2-배포)

---

## 1. Docker 로컬 실행

### 🎯 목표
로컬에서 Docker로 애플리케이션을 실행하여 "어디서든 동일하게 실행되는 환경" 구축

### 📁 파일 구조
```
kotlin-board-example/
├── Dockerfile              # Docker 이미지 빌드 설정
├── .dockerignore          # Docker 빌드 시 제외할 파일
└── build.gradle.kts
```

### 🔨 Dockerfile 설명

**멀티스테이지 빌드**를 사용하여 이미지 크기를 최소화합니다.

```dockerfile
# Stage 1: 빌드 단계 (Gradle로 JAR 생성)
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: 실행 단계 (경량 JRE만 포함)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", \
    "-Dspring.profiles.active=prod", \
    "-Xmx512m", \
    "-XX:+UseContainerSupport", \
    "-jar", \
    "app.jar"]
```

**왜 멀티스테이지?**
- Stage 1: Gradle + JDK (무거움, 빌드용)
- Stage 2: JRE만 (가벼움, 실행용)
- 결과: 이미지 크기 최적화 및 보안 강화
- 헬스체크 기능으로 컨테이너 상태 모니터링 가능

### 🚀 실행 방법

#### 1) Docker 이미지 빌드
```bash
docker build -t kotlin-board:latest .
```

**예상 시간**: 최초 5-10분 (이후 캐시로 1-2분)

#### 2) Docker 컨테이너 실행
```bash
docker run -d \
  --name kotlin-board \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  kotlin-board:latest
```

**옵션 설명**:
- `-d`: 백그라운드 실행
- `--name`: 컨테이너 이름 지정
- `-p 8080:8080`: 포트 매핑 (호스트:컨테이너)
- `-e`: 환경변수 설정

#### 3) 로그 확인
```bash
docker logs -f kotlin-board
```

#### 4) 접속 확인
```bash
curl http://localhost:8080/swagger-ui/index.html
```

#### 5) 컨테이너 중지 & 삭제
```bash
docker stop kotlin-board
docker rm kotlin-board
```

---

## 2. Docker Compose 구성

### 🎯 목표
MySQL + Spring Boot를 한 번에 실행 (개발 환경 & 프로덕션 환경 분리)

### 📁 docker-compose.yml (개발 환경)

Spring Boot를 핫 리로드 모드로 실행하여 코드 변경사항을 즉시 반영합니다.

```yaml
version: '3.8'

services:
  app:
    image: gradle:8.5-jdk17
    container_name: kotlin-board-dev
    working_dir: /app
    command: ./gradlew bootRun --no-daemon
    volumes:
      - .:/app
      - gradle-cache:/root/.gradle
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - GRADLE_OPTS=-Xmx512m
    networks:
      - board-network

volumes:
  gradle-cache:

networks:
  board-network:
    driver: bridge
```

### 📁 docker-compose.prod.yml (프로덕션 환경)

MySQL DB와 함께 최적화된 JAR 파일로 실행합니다.

```yaml
version: '3.8'

services:
  # MySQL 데이터베이스
  mysql:
    image: mysql:8.0
    container_name: kotlin-board-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: boarddb
      MYSQL_USER: boarduser
      MYSQL_PASSWORD: boardpassword
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - board-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Spring Boot 애플리케이션
  app:
    image: gradle:8.5-jdk17
    container_name: kotlin-board-prod
    working_dir: /app
    command: >
      sh -c "
        ./gradlew bootJar --no-daemon -x test &&
        java -Dspring.profiles.active=prod -Xmx512m -XX:+UseContainerSupport -jar build/libs/*.jar
      "
    volumes:
      - .:/app
      - gradle-cache:/root/.gradle
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/boarddb
      - SPRING_DATASOURCE_USERNAME=boarduser
      - SPRING_DATASOURCE_PASSWORD=boardpassword
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - board-network
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 3s
      retries: 3

volumes:
  mysql-data:
  gradle-cache:

networks:
  board-network:
    driver: bridge
```

### 🚀 실행 방법

#### 개발 환경 실행
```bash
# 개발 모드로 실행 (핫 리로드 활성화)
docker-compose up -d

# 로그 확인
docker-compose logs -f app

# 종료
docker-compose down
```

**접속**: http://localhost:8080/swagger-ui/index.html

#### 프로덕션 환경 실행
```bash
# 프로덕션 모드로 실행 (MySQL + 최적화된 JAR)
docker-compose -f docker-compose.prod.yml up -d

# 로그 확인
docker-compose -f docker-compose.prod.yml logs -f app

# 서비스 상태 확인
docker-compose -f docker-compose.prod.yml ps

# 종료
docker-compose -f docker-compose.prod.yml down

# 볼륨까지 삭제
docker-compose -f docker-compose.prod.yml down -v
```

**접속**: http://localhost:8080/swagger-ui/index.html

---

## 3. GitHub Actions CI 구축

### 🎯 목표
main 브랜치에 코드 푸시 → 자동으로 빌드 & 테스트

### 📁 워크플로우 파일 생성

`.github/workflows/ci.yml`:

```yaml
name: CI - Build and Test

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build-and-test:
    runs-on: ubuntu-latest

    steps:
      # 1. 코드 체크아웃
      - name: Checkout code
        uses: actions/checkout@v4

      # 2. JDK 17 설치
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'gradle'

      # 3. Gradle 권한 부여
      - name: Grant execute permission for gradlew
        run: chmod +x ./gradlew

      # 4. 빌드 & 테스트
      - name: Build and Test
        run: ./gradlew clean build

      # 5. 테스트 결과 업로드
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: build/reports/tests/test/

      # 6. JAR 파일 업로드 (다음 job에서 사용)
      - name: Upload JAR
        uses: actions/upload-artifact@v4
        with:
          name: application-jar
          path: build/libs/*.jar
```

### ✅ CI가 하는 일

1. ✅ **코드 체크아웃**: GitHub에서 최신 코드 가져오기
2. ✅ **JDK 설치**: Java 17 설치 (Gradle 캐시 활용)
3. ✅ **빌드**: `./gradlew clean build` 실행
4. ✅ **테스트**: 자동으로 모든 테스트 실행
5. ✅ **결과 저장**: 테스트 리포트 & JAR 파일 저장

### 📊 확인 방법

1. GitHub 리포지토리 → **Actions** 탭
2. 최근 워크플로우 실행 확인
3. 빌드 성공 ✅ / 실패 ❌ 상태 확인

---

## 4. GitHub Actions CD 구축

### 🎯 목표
테스트 통과 → Docker Hub에 이미지 푸시 → AWS EC2 자동 배포

### 📁 워크플로우 파일 업데이트

`.github/workflows/cd.yml`:

```yaml
name: CD - Deploy to AWS

on:
  push:
    branches: [ main ]

jobs:
  # Job 1: Docker 이미지 빌드 & 푸시
  docker-build-push:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      # Docker Buildx 설정 (멀티플랫폼 빌드)
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      # Docker Hub 로그인
      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      # 이미지 메타데이터 추출
      - name: Extract metadata
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ secrets.DOCKERHUB_USERNAME }}/kotlin-board
          tags: |
            type=sha,prefix={{branch}}-
            type=raw,value=latest,enable={{is_default_branch}}

      # Docker 이미지 빌드 & 푸시
      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          cache-from: type=gha
          cache-to: type=gha,mode=max

  # Job 2: AWS EC2 배포
  deploy-to-ec2:
    needs: docker-build-push
    runs-on: ubuntu-latest

    steps:
      # SSH로 EC2 접속 후 배포
      - name: Deploy to EC2
        uses: appleboy/ssh-action@v1.0.0
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ${{ secrets.EC2_USER }}
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            # Docker Hub에서 최신 이미지 pull
            docker pull ${{ secrets.DOCKERHUB_USERNAME }}/kotlin-board:latest

            # 기존 컨테이너 중지 & 삭제
            docker stop kotlin-board || true
            docker rm kotlin-board || true

            # 새 컨테이너 실행
            docker run -d \
              --name kotlin-board \
              -p 8080:8080 \
              -e SPRING_PROFILES_ACTIVE=prod \
              ${{ secrets.DOCKERHUB_USERNAME }}/kotlin-board:latest

            # 사용하지 않는 이미지 정리
            docker system prune -f
```

### 🔐 GitHub Secrets 설정

GitHub 리포지토리 → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

필요한 Secrets:
1. `DOCKERHUB_USERNAME`: Docker Hub 사용자명
2. `DOCKERHUB_TOKEN`: Docker Hub Access Token
3. `EC2_HOST`: EC2 퍼블릭 IP (예: `52.79.123.45`)
4. `EC2_USER`: EC2 사용자명 (예: `ubuntu`)
5. `EC2_SSH_KEY`: EC2 프라이빗 키 (`.pem` 파일 내용 전체)

### 📦 Docker Hub Token 생성

1. Docker Hub 로그인
2. **Account Settings** → **Security** → **New Access Token**
3. 토큰 이름: `github-actions`
4. 권한: `Read, Write, Delete`
5. 생성된 토큰 복사 → GitHub Secrets에 저장

### 📝 참고: 현재 프로젝트 DB 설정

현재 프로젝트는 **MySQL 8.0**을 사용합니다:
- 개발 환경: H2 인메모리 DB (application.yml)
- 프로덕션 환경: MySQL (application-prod.yml)
  - 기본 DB명: `boarddb`
  - 기본 사용자: `boarduser`
  - 환경변수로 설정 가능: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`

---

## 5. AWS EC2 배포

### 🎯 목표
실제 서버에 애플리케이션 배포

### 📋 EC2 인스턴스 생성

1. **AWS 콘솔** → **EC2** → **인스턴스 시작**
2. **AMI 선택**: Ubuntu Server 22.04 LTS
3. **인스턴스 타입**: t2.micro (프리티어)
4. **키 페어**: 새로 생성 (`kotlin-board.pem` 다운로드)
5. **보안 그룹**:
   - SSH (22) - 내 IP만
   - HTTP (80) - 모든 위치
   - Custom TCP (8080) - 모든 위치

### 🔧 EC2 초기 설정

```bash
# 1. SSH 접속
ssh -i kotlin-board.pem ubuntu@<EC2_PUBLIC_IP>

# 2. Docker 설치
sudo apt update
sudo apt install -y docker.io
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ubuntu

# 3. 재접속 (Docker 권한 적용)
exit
ssh -i kotlin-board.pem ubuntu@<EC2_PUBLIC_IP>

# 4. Docker 동작 확인
docker --version
```

### 🚀 수동 배포 테스트

```bash
# Docker Hub에서 이미지 pull
docker pull your-dockerhub-username/kotlin-board:latest

# 컨테이너 실행
docker run -d \
  --name kotlin-board \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  your-dockerhub-username/kotlin-board:latest

# 로그 확인
docker logs -f kotlin-board

# 브라우저에서 접속
http://<EC2_PUBLIC_IP>:8080/swagger-ui/index.html
```

---

## 🎉 전체 플로우 요약

```
[로컬 개발]
    ↓
[Git Push to main]
    ↓
[GitHub Actions CI]
- 자동 빌드
- 자동 테스트
    ↓ (성공 시)
[GitHub Actions CD]
- Docker 이미지 빌드
- Docker Hub에 푸시
    ↓
[AWS EC2]
- 최신 이미지 pull
- 기존 컨테이너 중지
- 새 컨테이너 실행
    ↓
[배포 완료!]
```

---

## 🐛 트러블슈팅

### 1. Docker 빌드 실패

**증상**: `./gradlew: Permission denied`

**해결**:
```bash
chmod +x ./gradlew
git add gradlew
git commit -m "Fix gradlew permission"
```

### 2. EC2 SSH 접속 실패

**증상**: `Permission denied (publickey)`

**해결**:
```bash
# .pem 파일 권한 변경
chmod 400 kotlin-board.pem

# SSH 접속
ssh -i kotlin-board.pem ubuntu@<EC2_PUBLIC_IP>
```

### 3. 포트 충돌

**증상**: `port is already allocated`

**해결**:
```bash
# 기존 컨테이너 중지
docker stop $(docker ps -aq)
docker rm $(docker ps -aq)
```

---

## 📚 참고 자료

- [Docker 공식 문서](https://docs.docker.com/)
- [GitHub Actions 문서](https://docs.github.com/en/actions)
- [AWS EC2 시작하기](https://aws.amazon.com/ec2/getting-started/)

---

## ✅ 체크리스트

- [ ] Dockerfile 작성 완료
- [ ] 로컬에서 Docker 실행 성공
- [ ] docker-compose.yml 작성 완료
- [ ] GitHub Actions CI 구축 완료
- [ ] Docker Hub 연동 완료
- [ ] EC2 인스턴스 생성 완료
- [ ] EC2에 Docker 설치 완료
- [ ] GitHub Actions CD 구축 완료
- [ ] 자동 배포 테스트 성공

---

**작성일**: 2025-12-04
**버전**: 1.0.0
