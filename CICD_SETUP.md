# 🚀 CI/CD 자동 배포 설정 가이드

이 문서는 **git push 한 번으로 EC2에 자동 배포**되도록 설정하는 방법을 설명합니다.

## 📋 전체 흐름

```
코드 수정 → git push origin main → GitHub Actions 자동 실행
  ↓
[CI] Gradle 빌드 → Docker 이미지 생성 → Docker Hub 푸시
  ↓
[CD] SSH로 EC2 접속 → 최신 이미지 pull → 컨테이너 재시작
  ↓
✅ 배포 완료! (서버 접속 불필요)
```

---

## 🏗️ 프로젝트 구조

### CI/CD 관련 파일
```
kotlin-board-example/
├── .github/
│   └── workflows/
│       └── deploy.yml              # GitHub Actions CI/CD 파이프라인
├── Dockerfile                      # Multi-stage 빌드 설정
├── docker-compose.prod.yml         # 운영 환경 Docker Compose
├── ec2-setup.sh                    # EC2 초기 설정 스크립트
└── .env                            # 환경 변수 (Git에 포함 안됨)
```

### 애플리케이션 구조
```
src/main/kotlin/com/example/board/
├── controller/                     # REST API 컨트롤러
│   ├── PostController.kt          # 게시글 API (테스트 엔드포인트 포함)
│   ├── CommentController.kt
│   ├── PostLikeController.kt
│   └── GlobalExceptionHandler.kt
├── service/                        # 비즈니스 로직
├── repository/                     # 데이터 접근 계층
├── entity/                         # JPA 엔티티
└── dto/                           # 데이터 전송 객체
```

---

## 1️⃣ GitHub Secrets 설정

GitHub 저장소에 다음 **5개의 Secrets**을 등록해야 합니다.

### 설정 위치
**GitHub 저장소 → Settings → Secrets and variables → Actions → New repository secret**

### 필수 Secrets

| Secret 이름 | 설명 | 예시 값 | 비고 |
|------------|------|--------|------|
| `DOCKER_USERNAME` | Docker Hub 사용자 이름 | `myusername` | 필수 |
| `DOCKER_PASSWORD` | Docker Hub 비밀번호 또는 Access Token | `dckr_pat_xxx...` | Access Token 권장 |
| `EC2_HOST` | EC2 퍼블릭 IP 주소 | `13.124.123.45` | 필수 |
| `EC2_USER` | EC2 SSH 사용자 이름 | `ec2-user` (Amazon Linux)<br>`ubuntu` (Ubuntu) | 필수 |
| `EC2_SSH_KEY` | EC2 SSH 프라이빗 키 (.pem 파일 내용) | 아래 참고 | 필수 |

### EC2_SSH_KEY 값 가져오기

```bash
# Mac/Linux
cat ~/Downloads/your-key.pem

# Windows (Git Bash)
cat /c/Users/YourName/Downloads/your-key.pem
```

출력된 **전체 내용**을 복사해서 GitHub Secret으로 등록합니다.

```
-----BEGIN RSA PRIVATE KEY-----
MIIEpAIBAAKCAQEA...
(여러 줄)
...
-----END RSA PRIVATE KEY-----
```

⚠️ **주의사항:**
- 시작(`-----BEGIN`)부터 끝(`-----END`)까지 전체 복사
- 앞뒤 공백 없이
- 줄바꿈 포함

---

## 2️⃣ Docker Hub Access Token 생성 (권장)

비밀번호 대신 Access Token 사용을 권장합니다.

1. **Docker Hub 로그인** → https://hub.docker.com/
2. **Account Settings → Security → New Access Token**
3. **Token description**: `GitHub Actions CI/CD`
4. **Access permissions**: `Read, Write, Delete`
5. **생성된 토큰을 `DOCKER_PASSWORD` Secret으로 등록**

---

## 3️⃣ EC2 서버 초기 설정 (최초 1회만)

### 3-1. EC2 보안 그룹 설정

**AWS 콘솔 → EC2 → 인스턴스 → 보안 탭 → 보안 그룹 → 인바운드 규칙 편집**

| 유형 | 프로토콜 | 포트 범위 | 소스 | 설명 |
|-----|---------|----------|------|------|
| SSH | TCP | 22 | `0.0.0.0/0` | GitHub Actions SSH 접속 |
| Custom TCP | TCP | 8080 | `0.0.0.0/0` | Spring Boot 애플리케이션 |
| MySQL/Aurora | TCP | 3306 | (선택) 내부만 | MySQL 접속 (선택사항) |

⚠️ **보안 경고**:
- SSH(22) 포트를 `0.0.0.0/0`으로 열면 모든 IP에서 접속 가능합니다.
- 프로덕션 환경에서는 VPN, Bastion Host 또는 AWS Systems Manager 사용을 권장합니다.

### 3-2. EC2에 SSH 접속

```bash
ssh -i your-key.pem ec2-user@YOUR_EC2_IP
```

### 3-3. Docker 및 Docker Compose 설치 확인

```bash
# Docker 설치 확인
docker --version

# Docker Compose 설치 확인
docker-compose --version

# 미설치 시 설치
sudo yum update -y
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# Docker Compose 설치
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 3-4. 프로젝트 디렉토리 생성

```bash
mkdir -p ~/kotlin-board-example
cd ~/kotlin-board-example
```

### 3-5. docker-compose.prod.yml 생성

```bash
cat > ~/kotlin-board-example/docker-compose.prod.yml << 'EOF'
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: kotlin-board-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
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

  app:
    image: ${DOCKER_ID}/kotlin-board:latest
    container_name: kotlin-board-prod
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL}
      - SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
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
    driver: local

networks:
  board-network:
    driver: bridge
EOF
```

### 3-6. .env 파일 생성 및 설정

```bash
vi ~/kotlin-board-example/.env
```

다음 내용을 입력하고 **실제 값으로 수정**:

```env
# Docker Hub 설정 (GitHub Secret의 DOCKER_USERNAME과 동일해야 함)
DOCKER_ID=your_docker_hub_username

# MySQL 설정
MYSQL_ROOT_PASSWORD=strong_root_password_123
MYSQL_DATABASE=boarddb
MYSQL_USER=boarduser
MYSQL_PASSWORD=strong_password_456

# Spring Boot 설정
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/boarddb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
SPRING_DATASOURCE_USERNAME=boarduser
SPRING_DATASOURCE_PASSWORD=strong_password_456
```

⚠️ **중요:**
- `DOCKER_ID`는 GitHub Secret의 `DOCKER_USERNAME`과 **정확히 동일**해야 합니다.
- 강력한 비밀번호 사용 (최소 12자, 특수문자 포함)

### 3-7. Docker Hub 로그인

```bash
docker login
# Username: your_docker_hub_username
# Password: (Docker Hub 비밀번호 또는 Access Token)
```

### 3-8. MySQL 먼저 시작

```bash
cd ~/kotlin-board-example
docker-compose -f docker-compose.prod.yml up -d mysql

# MySQL 준비 완료 확인 (10-20초 대기)
docker logs kotlin-board-mysql
```

성공 메시지 확인:
```
[System] [MY-010931] [Server] /usr/sbin/mysqld: ready for connections.
```

### 3-9. 애플리케이션 수동 배포 (최초 1회)

```bash
# 이미지 가져오기
docker-compose -f docker-compose.prod.yml pull app

# 애플리케이션 시작
docker-compose -f docker-compose.prod.yml up -d app

# 확인
docker ps
docker logs kotlin-board-prod -f
```

### 3-10. 배포 확인

```bash
# 헬스체크
curl http://localhost:8080/actuator/health

# 버전 확인 (배포 테스트용 엔드포인트)
curl http://localhost:8080/api/posts/kt/version
```

---

## 4️⃣ CI/CD 파이프라인 이해하기

### GitHub Actions 워크플로우 (.github/workflows/deploy.yml)

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ "main" ]  # main 브랜치 push 시 자동 실행

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest

    steps:
      # CI: 빌드 & 도커 이미지 생성
      - 코드 가져오기
      - JDK 17 설치
      - Gradle 빌드 (테스트 제외)
      - Docker Hub 로그인
      - Docker 이미지 빌드 및 푸시

      # CD: EC2 서버에 자동 배포
      - SSH로 EC2 접속
      - docker-compose pull (최신 이미지 가져오기)
      - docker-compose up -d (컨테이너 재시작)
      - 이전 이미지 정리
```

### Dockerfile (Multi-stage 빌드)

**Stage 1: Build**
- Gradle로 애플리케이션 빌드
- 의존성 캐싱으로 빌드 속도 향상
- JAR 파일 생성

**Stage 2: Runtime**
- 경량화된 JRE 이미지 사용
- JAR 파일만 복사
- 헬스체크 설정
- 최종 이미지 크기 최소화

---

## 5️⃣ 자동 배포 테스트

### 5-1. 코드 수정

```bash
# 테스트용 파일 수정
echo "# CI/CD Test" >> README.md
```

### 5-2. 커밋 & 푸시

```bash
git add .
git commit -m "test: CI/CD 자동 배포 테스트"
git push origin main
```

### 5-3. GitHub Actions 확인

1. **GitHub 저장소 → Actions 탭**
2. **"CI/CD Pipeline" 워크플로우 클릭**
3. **실시간 로그 확인**

예상 소요 시간:
- 빌드: 2-3분
- Docker 푸시: 30초-1분
- EC2 배포: 30초-1분
- **총 4-5분**

### 5-4. 배포 성공 확인

**방법 1: 브라우저에서 확인**
```
http://YOUR_EC2_IP:8080/api/posts/kt/version
```

응답 예시:
```json
{
  "version": "1.0.1",
  "deployedAt": "2025-12-05T14:30:00",
  "status": "CI/CD 자동 배포 성공!",
  "message": "이 엔드포인트가 보이면 자동 배포가 완료된 것입니다!"
}
```

**방법 2: curl 명령어**
```bash
curl http://YOUR_EC2_IP:8080/api/posts/kt/version
```

**방법 3: 헬스체크**
```bash
curl http://YOUR_EC2_IP:8080/actuator/health
```

**방법 4: EC2에서 직접 확인**
```bash
ssh -i your-key.pem ec2-user@YOUR_EC2_IP
docker ps
docker logs kotlin-board-prod --tail 50
```

---

## 6️⃣ API 문서 확인

배포 완료 후 Swagger UI에서 API 문서를 확인할 수 있습니다:

```
http://YOUR_EC2_IP:8080/swagger-ui/index.html
```

**사용 가능한 API:**
- 게시글 CRUD (`/api/posts/kt`)
- 댓글 CRUD (`/api/comments/kt`)
- 좋아요 관리 (`/api/post-likes/kt`)
- 배포 테스트 (`/api/posts/kt/version`)

---

## 🔧 트러블슈팅

### 1. SSH 연결 실패
```
Error: dial tcp x.x.x.x:22: i/o timeout
```

**원인**: EC2 보안 그룹에서 SSH(22) 포트가 막혀있음

**해결**:
1. AWS 콘솔 → EC2 → 보안 그룹
2. 인바운드 규칙 편집
3. SSH(22) 규칙 추가 (소스: `0.0.0.0/0`)

### 2. Docker Hub 로그인 실패
```
Error: unauthorized: incorrect username or password
```

**원인**: GitHub Secrets의 `DOCKER_USERNAME` 또는 `DOCKER_PASSWORD` 오류

**해결**:
1. Docker Hub 로그인 정보 확인
2. Access Token 사용 권장
3. GitHub Secrets 재등록

### 3. 이미지 pull 실패
```
Error: pull access denied, repository does not exist
```

**원인**: EC2의 `.env` 파일의 `DOCKER_ID`가 GitHub Secret과 다름

**해결**:
```bash
# EC2에서 확인
cat ~/kotlin-board-example/.env | grep DOCKER_ID

# 수정
vi ~/kotlin-board-example/.env
```

### 4. 컨테이너 시작 실패
```bash
# EC2에서 로그 확인
docker logs kotlin-board-prod

# 일반적인 원인:
# 1. MySQL이 준비되지 않음
docker-compose -f docker-compose.prod.yml up -d mysql
docker logs kotlin-board-mysql

# 2. 환경 변수 오류
cat .env

# 3. 포트 충돌
docker ps
sudo lsof -i :8080
```

### 5. MySQL 연결 실패
```
Communications link failure
```

**원인**: MySQL 컨테이너가 완전히 시작되지 않음

**해결**:
```bash
# MySQL 로그 확인
docker logs kotlin-board-mysql

# 재시작
docker-compose -f docker-compose.prod.yml restart mysql

# 10-20초 대기 후 애플리케이션 재시작
docker-compose -f docker-compose.prod.yml restart app
```

### 6. GitHub Actions 빌드 실패
```
Execution failed for task ':bootJar'
```

**원인**: 코드 컴파일 오류 또는 의존성 문제

**해결**:
```bash
# 로컬에서 빌드 테스트
./gradlew clean build

# 실패 시 로그 확인
./gradlew build --stacktrace
```

### 7. 배포 후 이전 버전이 보임
```bash
# EC2에서 확인
docker images | grep kotlin-board

# 강제로 최신 이미지 가져오기
docker-compose -f docker-compose.prod.yml pull app
docker-compose -f docker-compose.prod.yml up -d app --force-recreate

# 캐시 전체 삭제
docker system prune -a
```

---

## 🎯 배포 플로우 최적화

### 다운타임 제로 배포 (Blue-Green)

현재 설정은 간단한 롤링 배포입니다. 다운타임을 최소화하려면:

1. **Nginx 리버스 프록시 사용**
2. **헬스체크 기반 트래픽 전환**
3. **Docker Compose의 `scale` 기능 활용**

### 롤백 전략

배포 실패 시 이전 버전으로 롤백:

```bash
# EC2에서 실행
cd ~/kotlin-board-example

# 특정 버전으로 롤백
docker-compose -f docker-compose.prod.yml down
docker pull your_docker_hub_username/kotlin-board:v1.0.0
docker tag your_docker_hub_username/kotlin-board:v1.0.0 your_docker_hub_username/kotlin-board:latest
docker-compose -f docker-compose.prod.yml up -d app
```

### 빌드 시간 단축

**현재**: 4-5분
**목표**: 2-3분

방법:
1. GitHub Actions 캐시 사용
2. 멀티 스테이지 빌드 최적화
3. Gradle 빌드 캐시 활용

---

## 🔐 보안 권장사항

### 1. SSH 키 관리
- ✅ `.pem` 파일 절대 Git에 커밋하지 말 것
- ✅ GitHub Secrets에만 저장
- ✅ 정기적으로 SSH 키 교체

### 2. 비밀번호 관리
- ✅ 강력한 비밀번호 사용 (12자 이상, 특수문자 포함)
- ✅ `.env` 파일 절대 Git에 커밋하지 말 것 (`.gitignore` 확인)
- ✅ AWS Secrets Manager 사용 고려

### 3. EC2 보안
- ✅ 불필요한 포트는 닫기
- ✅ SSH 키 인증만 사용 (비밀번호 인증 비활성화)
- ✅ 정기적인 보안 업데이트
- ✅ VPN 또는 Bastion Host 사용

### 4. Docker Hub
- ✅ Access Token 사용 (비밀번호 대신)
- ✅ Private Repository 사용 고려
- ✅ 이미지 태깅 전략 (latest 외에 버전 태그)

### 5. 애플리케이션 보안
- ✅ HTTPS 설정 (Let's Encrypt)
- ✅ Spring Security 설정
- ✅ 정기적인 의존성 업데이트

---

## 📚 참고 자료

### 공식 문서
- [GitHub Actions](https://docs.github.com/en/actions)
- [Docker Hub](https://hub.docker.com/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Docker Multi-stage Builds](https://docs.docker.com/build/building/multi-stage/)

### GitHub Actions
- [appleboy/ssh-action](https://github.com/appleboy/ssh-action)
- [docker/login-action](https://github.com/docker/login-action)

### 프로젝트 문서
- `README.md` - 프로젝트 전체 개요
- `KOTLIN_GUIDE.md` - Kotlin 학습 가이드
- `DOCKER_CICD.md` - Docker 상세 가이드
- `claude.md` - 프로젝트 컨텍스트 (Claude Code용)

---

## 🎉 완료!

이제 **코드만 수정하고 git push**하면:
- ✅ GitHub Actions가 자동으로 빌드
- ✅ Docker 이미지 생성 및 푸시
- ✅ EC2에 자동 배포
- ✅ 컨테이너 자동 재시작

**더 이상 AWS 콘솔에 들어가서 수동으로 배포할 필요가 없습니다!**

---

### 문의 및 피드백
- GitHub Issues: https://github.com/dbswhd4932/kotlin-board/issues
- 프로젝트 저장소: https://github.com/dbswhd4932/kotlin-board
