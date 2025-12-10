# Kotlin Board API

Spring Boot와 Kotlin을 활용한 게시판 REST API 프로젝트입니다. Coroutines를 활용한 비동기 처리와 GitHub Actions를 통한 자동 배포를 지원합니다.

## 🎯 프로젝트 특징

- ✅ **Kotlin 완전 변환**: Java에서 Kotlin으로 100% 리팩토링 완료
- ✅ **Coroutines 비동기 처리**: suspend 함수와 async/await를 활용한 성능 최적화
- ✅ **CI/CD 자동 배포**: GitHub Actions → Docker Hub → AWS EC2 자동 배포
- ✅ **QueryDSL 동적 쿼리**: 타입 안전한 동적 쿼리 작성
- ✅ **API 문서화**: Swagger/OpenAPI 자동 생성

## 🛠 기술 스택

### Backend
- **Kotlin** 2.0.21
- **Spring Boot** 3.2.0
- **Spring Data JPA** + **QueryDSL** 5.0.0
- **Coroutines** 1.7.3 (비동기 처리)

### Database
- **H2** (개발 환경)
- **MySQL** 8.0 (운영 환경)

### DevOps
- **Docker** + **Docker Compose**
- **GitHub Actions** (CI/CD)
- **AWS EC2** (배포)
- **Nginx** + **Certbot** (리버스 프록시, HTTPS)

## 📁 프로젝트 구조

```
kotlin-board-example/
├── .github/
│   └── workflows/
│       └── deploy.yml              # CI/CD 파이프라인
├── src/
│   └── main/
│       ├── kotlin/                 # Kotlin 소스 코드
│       │   └── com/example/board/
│       │       ├── entity/         # JPA 엔티티
│       │       ├── dto/            # 요청/응답 DTO
│       │       ├── repository/     # JPA + QueryDSL
│       │       ├── service/        # 비즈니스 로직 (Coroutines)
│       │       ├── controller/     # REST API
│       │       └── config/         # 설정 (Swagger)
│       └── resources/
│           ├── application.yml     # 공통 설정
│           ├── application-dev.yml # 개발 환경 (H2)
│           └── application-prod.yml # 운영 환경 (MySQL)
├── Dockerfile                      # Multi-stage 빌드
├── docker-compose.prod.yml         # 운영 환경 (MySQL + App + Nginx)
└── build.gradle.kts               # Gradle 빌드 설정
```

## 🔑 핵심 Kotlin 기능

### 1. Data Class (간결한 DTO)
```kotlin
// Java: 수십 줄의 boilerplate 코드 필요
// Kotlin: 단 3줄
data class PostResponse(
    val id: Long,
    val title: String,
    val content: String,
    val author: String
) // equals, hashCode, toString, copy 자동 생성
```

### 2. Null Safety (NPE 방지)
```kotlin
// Nullable 타입 명시
val post: Post? = repository.findByIdOrNull(id)

// Safe call & Elvis operator
val title = post?.title ?: "제목 없음"

// let으로 null 체크
post?.let {
    println(it.title)
}
```

### 3. 생성자 주입 간소화
```kotlin
@Service
class PostService(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository
) {
    // 생성자와 필드 선언을 한 번에!
}
```

### 4. 컬렉션 함수형 프로그래밍
```kotlin
// Stream API 없이 간결하게
val posts = postList
    .filter { it.published }
    .map { PostResponse.from(it) }
    .sortedByDescending { it.createdAt }
```

### 5. Coroutines 비동기 처리
```kotlin
// 동기 방식 (순차 실행)
fun getPost(id: Long): PostDetailResponse {
    val post = postRepository.findById(id)      // 1초
    val comments = commentRepository.find(id)   // 1초
    val likes = likeRepository.count(id)        // 1초
    // 총 3초 소요
}

// 비동기 방식 (병렬 실행)
suspend fun getPost(id: Long): PostDetailResponse = coroutineScope {
    val postDeferred = async { postRepository.findById(id) }
    val commentsDeferred = async { commentRepository.find(id) }
    val likesDeferred = async { likeRepository.count(id) }

    PostDetailResponse(
        postDeferred.await(),
        commentsDeferred.await(),
        likesDeferred.await()
    )
    // 총 1초 소요 (병렬 실행으로 3배 빠름!)
}
```

### 6. 확장 함수 (기존 클래스에 메서드 추가)
```kotlin
// PageRequest DTO를 Pageable로 변환하는 확장 함수
fun PostDto.PageRequest.toPageable(): Pageable {
    val sort = Sort.by(
        if (direction == ASCENDING) Sort.Direction.ASC
        else Sort.Direction.DESC,
        sortBy
    )
    return PageRequest.of(page, size, sort)
}

// 사용
val pageable = request.toPageable()
```

## 📡 API 엔드포인트

### 게시글 API (`/api/posts/kt`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/` | 게시글 목록 (페이징, 정렬) |
| GET | `/{id}` | 게시글 상세 (댓글 포함) |
| GET | `/search?keyword=...` | 게시글 검색 (제목+내용) |
| POST | `/` | 게시글 생성 |
| PUT | `/{id}` | 게시글 수정 |
| DELETE | `/{id}` | 게시글 삭제 |
| GET | `/{id}/sync` | **동기 방식 조회** (성능 비교용) |
| GET | `/{id}/async` | **비동기 방식 조회** (Coroutines) |
| GET | `/version` | **배포 버전 확인** (CI/CD 테스트용) |

### 댓글 API (`/api/comments/kt`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/post/{postId}` | 특정 게시글의 댓글 목록 |
| POST | `/` | 댓글 생성 |
| PUT | `/{id}` | 댓글 수정 |
| DELETE | `/{id}` | 댓글 삭제 |

### 좋아요 API (`/api/post-likes/kt`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/` | 좋아요 추가 |
| DELETE | `/{id}` | 좋아요 취소 |
| GET | `/post/{postId}` | 게시글의 좋아요 수 |

### API 문서
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **Health Check**: `http://localhost:8080/actuator/health`

## 🚀 빠른 시작

### 로컬 실행 (개발 환경)
```bash
# 1. 빌드
./gradlew build

# 2. 실행 (H2 DB 사용)
./gradlew bootRun --args='--spring.profiles.active=dev'

# 3. API 테스트
curl http://localhost:8080/api/posts/kt
```

### Docker Compose 실행
```bash
# 개발 환경 (H2)
docker-compose -f docker-compose.dev.yml up

# 운영 환경 (MySQL + Nginx)
docker-compose -f docker-compose.prod.yml up -d
```

### API 테스트 예제
```bash
# 게시글 생성
curl -X POST http://localhost:8080/api/posts/kt \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Kotlin은 최고!",
    "content": "Coroutines로 비동기 처리가 쉬워요",
    "author": "개발자"
  }'

# 게시글 목록 조회 (페이징)
curl "http://localhost:8080/api/posts/kt?page=0&size=10&sortBy=createdAt&direction=DESCENDING"

# 게시글 검색
curl "http://localhost:8080/api/posts/kt/search?keyword=Kotlin"

# 성능 비교: 동기 vs 비동기
curl http://localhost:8080/api/posts/kt/1/sync   # 동기 방식
curl http://localhost:8080/api/posts/kt/1/async  # 비동기 방식 (더 빠름!)
```

## 🔄 CI/CD 자동 배포

### 배포 파이프라인
```
코드 Push (main 브랜치)
    ↓
GitHub Actions 트리거
    ↓
┌─────────────────────────────┐
│  CI (빌드 & 도커화)          │
│  1. Gradle 빌드              │
│  2. JAR 파일 생성             │
│  3. Docker 이미지 빌드        │
│  4. Docker Hub 푸시           │
└─────────────────────────────┘
    ↓
┌─────────────────────────────┐
│  CD (배포)                   │
│  1. EC2 SSH 접속             │
│  2. 이미지 Pull               │
│  3. 컨테이너 재시작           │
│  4. 헬스체크                  │
└─────────────────────────────┘
    ↓
✅ 자동 배포 완료!
```

### GitHub Secrets 설정 (필수)
```bash
DOCKER_USERNAME      # Docker Hub 사용자명
DOCKER_PASSWORD      # Docker Hub 액세스 토큰
EC2_HOST            # EC2 퍼블릭 IP
EC2_USERNAME        # SSH 사용자 (ec2-user 또는 ubuntu)
EC2_SSH_KEY         # EC2 SSH 프라이빗 키 (.pem 파일 내용)
```

### 배포 확인
```bash
# 배포 버전 확인 API
curl http://YOUR_EC2_IP/api/posts/kt/version

# 응답 예시
{
  "version": "1.0.1",
  "deployedAt": "2025-12-09T12:30:00",
  "status": "CI/CD 자동 배포 성공!",
  "message": "이 엔드포인트가 보이면 자동 배포가 완료된 것입니다!"
}
```

### 주요 특징
- ✅ **캐시 무효화**: 빌드 인자(타임스탬프 + 커밋 해시)로 이미지 강제 갱신
- ✅ **무중단 배포**: Docker Compose의 컨테이너 재시작
- ✅ **자동 헬스체크**: Spring Boot Actuator 활용
- ✅ **Multi-stage 빌드**: 경량 Docker 이미지 생성

상세 가이드: `CICD_SETUP.md`, `DOCKER_CICD.md` 참고

---

## 📚 참고 문서

- **CLAUDE.md** - 전체 프로젝트 상세 가이드
- **KOTLIN_GUIDE.md** - Kotlin 학습 자료
- **CICD_SETUP.md** - CI/CD 설정 가이드
- **DOCKER_CICD.md** - Docker 사용법
- **API_EXAMPLES.md** - API 사용 예제
- **TESTING_GUIDE.md** - 테스트 가이드

