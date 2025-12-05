# 🚀 CI/CD 자동 배포 설정 가이드

이 문서는 **git push 한 번으로 EC2에 자동 배포**되도록 설정하는 방법을 설명합니다.

## 📋 전체 흐름

```
코드 수정 → git push → GitHub Actions 실행
  ↓
Gradle 빌드 → Docker 이미지 생성 → Docker Hub 푸시
  ↓
SSH로 EC2 접속 → 최신 이미지 pull → 컨테이너 재시작
  ↓
✅ 배포 완료! (AWS 콘솔 접속 불필요)
```

---

## 1️⃣ GitHub Secrets 설정

GitHub 저장소에 다음 **5개의 Secrets**를 등록해야 합니다.

### GitHub 저장소 → Settings → Secrets and variables → Actions → New repository secret

| Secret 이름 | 설명 | 예시 값 |
|------------|------|--------|
| `DOCKER_USERNAME` | Docker Hub 사용자 이름 | `myusername` |
| `DOCKER_PASSWORD` | Docker Hub 비밀번호 또는 Access Token | `dckr_pat_xxx...` |
| `EC2_HOST` | EC2 퍼블릭 IP 주소 | `13.124.123.45` |
| `EC2_USER` | EC2 SSH 사용자 이름 | `ec2-user` (Amazon Linux)<br>`ubuntu` (Ubuntu) |
| `EC2_SSH_KEY` | EC2 SSH 프라이빗 키 (.pem 파일 내용) | 아래 참고 |

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

---

## 2️⃣ EC2 서버 초기 설정 (최초 1회만)

### 2-1. EC2에 SSH 접속

```bash
ssh -i your-key.pem ec2-user@your-ec2-ip
```

### 2-2. 설정 스크립트 실행

```bash
# 스크립트 다운로드
curl -o ec2-setup.sh https://raw.githubusercontent.com/YOUR_USERNAME/kotlin-board-example/main/ec2-setup.sh

# 실행 권한 부여
chmod +x ec2-setup.sh

# 실행
./ec2-setup.sh
```

또는 **직접 명령어 실행:**

```bash
# 1. 프로젝트 디렉토리 생성
mkdir -p ~/kotlin-board-example
cd ~/kotlin-board-example

# 2. docker-compose.prod.yml 다운로드
curl -o docker-compose.prod.yml https://raw.githubusercontent.com/YOUR_USERNAME/kotlin-board-example/main/docker-compose.prod.yml

# 3. .env 파일 생성 (아래 예시 참고)
vi .env
```

### 2-3. .env 파일 설정

```bash
vi ~/kotlin-board-example/.env
```

다음 내용을 입력하고 **실제 값으로 수정**합니다:

```env
# Docker Hub 설정
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

### 2-4. Docker Hub 로그인

```bash
docker login
# Username: your_docker_hub_username
# Password: (입력)
```

### 2-5. MySQL 먼저 시작

```bash
cd ~/kotlin-board-example
docker-compose -f docker-compose.prod.yml up -d mysql

# MySQL 준비 완료 확인 (10초 정도 대기)
docker logs kotlin-board-mysql
```

### 2-6. 애플리케이션 시작

```bash
docker-compose -f docker-compose.prod.yml up -d app

# 확인
docker ps
curl http://localhost:8080/actuator/health
```

---

## 3️⃣ EC2 보안 그룹 설정

AWS 콘솔에서 EC2 인스턴스의 **보안 그룹** 인바운드 규칙 확인:

| 유형 | 프로토콜 | 포트 | 소스 | 설명 |
|-----|---------|------|------|------|
| SSH | TCP | 22 | GitHub Actions IP | GitHub Actions가 SSH 접속 |
| Custom TCP | TCP | 8080 | 0.0.0.0/0 | 애플리케이션 외부 접근 |
| MySQL | TCP | 3306 | 내부 전용 | (선택) 외부 접근 불필요 |

**참고:** GitHub Actions는 고정 IP가 없으므로 SSH(22)는 `0.0.0.0/0` 또는 신뢰할 수 있는 IP 범위로 설정해야 합니다.

보안 강화 방법:
- VPN 사용
- Bastion Host 경유
- AWS Systems Manager Session Manager 사용 (SSH 22 불필요)

---

## 4️⃣ 자동 배포 테스트

### 테스트 순서

1. **코드 수정**
```bash
# 아무 파일이나 수정
echo "# Test CI/CD" >> README.md
```

2. **커밋 & 푸시**
```bash
git add .
git commit -m "test: CI/CD 자동 배포 테스트"
git push origin main
```

3. **GitHub Actions 확인**
- GitHub 저장소 → Actions 탭
- 실시간으로 빌드/배포 진행 상황 확인

4. **EC2에서 배포 확인**
```bash
# EC2에 접속하지 않아도 되지만, 확인하려면:
ssh -i your-key.pem ec2-user@your-ec2-ip
docker ps
docker logs kotlin-board-prod
```

5. **브라우저에서 확인**
```
http://your-ec2-ip:8080/actuator/health
```

---

## 🎯 완료!

이제부터는 **코드만 수정하고 git push**하면:
- ✅ GitHub Actions가 자동으로 빌드
- ✅ Docker 이미지 생성 및 푸시
- ✅ EC2에 자동 배포
- ✅ 컨테이너 자동 재시작

**AWS 콘솔에 들어가서 수동으로 배포할 필요가 없습니다!**

---

## 🔧 트러블슈팅

### SSH 연결 실패
```
Error: dial tcp x.x.x.x:22: i/o timeout
```
→ EC2 보안 그룹에서 SSH(22) 포트 인바운드 규칙 확인

### Docker Hub 로그인 실패
```
Error response from daemon: unauthorized
```
→ GitHub Secrets의 `DOCKER_USERNAME`, `DOCKER_PASSWORD` 확인

### 이미지 pull 실패
```
Error: image not found
```
→ `.env` 파일의 `DOCKER_ID` 확인 (GitHub Secret과 동일해야 함)

### 컨테이너 시작 실패
```bash
# EC2에서 로그 확인
docker logs kotlin-board-prod

# 일반적인 원인:
# 1. MySQL이 준비되지 않음 → MySQL 먼저 시작
# 2. .env 파일 오류 → 환경 변수 확인
# 3. 포트 충돌 → docker ps로 포트 사용 확인
```

### MySQL 연결 실패
```
Communications link failure
```
→ MySQL 컨테이너가 완전히 시작될 때까지 대기 (10-20초)

---

## 📚 참고 자료

- [GitHub Actions 공식 문서](https://docs.github.com/en/actions)
- [Docker Hub](https://hub.docker.com/)
- [appleboy/ssh-action](https://github.com/appleboy/ssh-action)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

---

## 🔐 보안 권장사항

1. **SSH 키 관리**
   - .pem 파일 절대 저장소에 커밋하지 말 것
   - GitHub Secrets에만 저장

2. **비밀번호 관리**
   - 강력한 비밀번호 사용
   - .env 파일 절대 커밋하지 말 것 (.gitignore 확인)

3. **EC2 보안**
   - 불필요한 포트는 닫기
   - SSH 키 인증만 사용 (비밀번호 인증 비활성화)
   - 정기적인 보안 업데이트

4. **Docker Hub**
   - Access Token 사용 권장 (비밀번호 대신)
   - Private Repository 사용 고려
