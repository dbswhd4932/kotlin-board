# Kotlin Board API 프로젝트

## 📋 프로젝트 개요

Java에서 Kotlin으로 코드를 변환하면서 학습하는 Spring Boot 게시판 API 프로젝트입니다.

### 주요 특징
- **Java to Kotlin 변환 학습**: Java 원본과 Kotlin 변환 코드 비교
- **REST API**: 게시글, 댓글, 좋아요 기능
- **CI/CD 자동 배포**: GitHub Actions + Docker + EC2
- **API 문서화**: Swagger/OpenAPI
- **성능 비교**: 동기 vs Coroutines 비동기 처리

---

## 🏗️ 프로젝트 구조

```
kotlin-board-example/
├── .github/
│   └── workflows/
│       └── deploy.yml              # CI/CD 파이프라인
├── src/
│   └── main/
│       ├── java/                   # Java 원본 코드 (학습용)
│       ├── kotlin/                 # Kotlin 변환 코드 (메인)
│       │   └── com/example/board/
│       │       ├── controller/     # REST API 컨트롤러
│       │       ├── service/        # 비즈니스 로직
│       │       ├── repository/     # 데이터 접근 (JPA, QueryDSL)
│       │       ├── entity/         # JPA 엔티티
│       │       ├── dto/            # 데이터 전송 객체
│       │       └── config/         # 설정 (Swagger 등)
│       └── resources/
│           ├── application.yml
│           ├── application-dev.yml
│           └── application-prod.yml
├── Dockerfile                      # Multi-stage 빌드
├── docker-compose.prod.yml         # 운영 환경 Docker Compose
├── docker-compose.dev.yml          # 개발 환경 Docker Compose
├── build.gradle.kts               # Gradle 빌드 설정
└── 문서/
    ├── CICD_SETUP.md              # CI/CD 설정 가이드
    ├── DOCKER_CICD.md             # Docker 상세 가이드
    ├── KOTLIN_GUIDE.md            # Kotlin 학습 가이드
    └── README.md                  # 프로젝트 전체 개요
```

---

## 🔄 Java to Kotlin 변환 체크리스트

### ✅ 완료된 변환

#### Entity
- [x] **Post** - 게시글 엔티티
- [x] **Comment** - 댓글 엔티티
- [x] **PostLike** - 좋아요 엔티티

#### DTO
- [x] **PostDto** - 게시글 DTO (Request/Response)
- [x] **CommentDto** - 댓글 DTO
- [x] **PostLikeDto** - 좋아요 DTO
- [x] **Extensions** - DTO 확장 함수

#### Repository
- [x] **PostRepository** - 게시글 기본 리포지토리
- [x] **CommentRepository** - 댓글 리포지토리
- [x] **PostLikeRepository** - 좋아요 리포지토리
- [x] **PostRepositoryCustom** - QueryDSL 커스텀 인터페이스
- [x] **PostRepositoryImpl** - QueryDSL 구현체

#### Service
- [x] **PostService** - 게시글 서비스 (동기/비동기 처리)
- [x] **CommentService** - 댓글 서비스

#### Controller
- [x] **PostController** - 게시글 API (테스트 엔드포인트 포함)
- [x] **CommentController** - 댓글 API
- [x] **PostLikeController** - 좋아요 API
- [x] **GlobalExceptionHandler** - 전역 예외 처리

#### Config
- [x] **SwaggerConfig** - API 문서화 설정

---

## 🔑 주요 Kotlin 변환 포인트

### 1. Optional → nullable
```kotlin
// Java
Post post = repository.findById(id).orElseThrow(() ->
    new EntityNotFoundException("게시글을 찾을 수 없습니다")
);

// Kotlin
val post = repository.findById(id)
    ?: throw EntityNotFoundException("게시글을 찾을 수 없습니다")
```

### 2. Stream API → Collection 함수
```kotlin
// Java
List<PostDto> dtos = posts.stream()
    .map(PostDto::from)
    .collect(Collectors.toList());

// Kotlin
val dtos = posts.map { PostDto.from(it) }
```

### 3. null 안전성
```kotlin
// Java
if (post != null) {
    System.out.println(post.getTitle());
}

// Kotlin
post?.let { println(it.title) }
```

### 4. 데이터 클래스
```kotlin
// Java
public class PostDto {
    private Long id;
    private String title;
    // getter, setter, equals, hashCode, toString...
}

// Kotlin
data class PostDto(
    val id: Long,
    val title: String
)
```

### 5. 확장 함수
```kotlin
// Pageable 변환 확장 함수
fun PostDto.PageRequest.toPageable(): Pageable {
    val sort = Sort.by(
        if (direction == PostDto.SortDirection.ASCENDING)
            Sort.Direction.ASC
        else
            Sort.Direction.DESC,
        sortBy
    )
    return PageRequest.of(page, size, sort)
}
```

### 6. Sealed Class (타입 안전한 열거형)
```kotlin
enum class SortDirection {
    ASCENDING, DESCENDING
}
```

### 7. Coroutines (비동기 처리)
```kotlin
// 동기 방식
fun getPostSync(id: Long): PostDetailResponse {
    val post = findPostById(id)
    val comments = commentRepository.findByPostId(id)
    val likes = postLikeRepository.countByPostId(id)
    return PostDetailResponse(post, comments, likes)
}

// Coroutines 비동기 방식
suspend fun getPostAsync(id: Long): PostDetailResponse = coroutineScope {
    val postDeferred = async { findPostById(id) }
    val commentsDeferred = async { commentRepository.findByPostId(id) }
    val likesDeferred = async { postLikeRepository.countByPostId(id) }

    PostDetailResponse(
        postDeferred.await(),
        commentsDeferred.await(),
        likesDeferred.await()
    )
}
```

---

## 🚀 CI/CD 파이프라인

### 전체 흐름
```
코드 수정 → git push → GitHub Actions
  ↓
[CI] Gradle 빌드 → Docker 이미지 생성 → Docker Hub 푸시
  ↓
[CD] SSH로 EC2 접속 → docker-compose pull → 컨테이너 재시작
  ↓
✅ 자동 배포 완료
```

### GitHub Actions 워크플로우
- **트리거**: `main` 브랜치 push
- **빌드**: JDK 17 + Gradle
- **도커화**: Multi-stage Dockerfile
- **배포**: SSH를 통한 EC2 자동 배포

### 필요한 GitHub Secrets
1. `DOCKER_USERNAME` - Docker Hub 사용자명
2. `DOCKER_PASSWORD` - Docker Hub 액세스 토큰
3. `EC2_HOST` - EC2 퍼블릭 IP
4. `EC2_USER` - SSH 사용자 (ec2-user 또는 ubuntu)
5. `EC2_SSH_KEY` - EC2 SSH 프라이빗 키 (.pem 파일)

### 배포 테스트 엔드포인트
```
GET /api/posts/kt/version
```

응답:
```json
{
  "version": "1.0.1",
  "deployedAt": "2025-12-05T14:30:00",
  "status": "CI/CD 자동 배포 성공!",
  "message": "이 엔드포인트가 보이면 자동 배포가 완료된 것입니다!"
}
```

---

## 🐳 Docker 설정

### Multi-stage Dockerfile
- **Stage 1 (Build)**: Gradle로 JAR 빌드
- **Stage 2 (Runtime)**: JRE만 포함한 경량 이미지

### Docker Compose
- **개발**: `docker-compose.dev.yml` (H2 DB)
- **운영**: `docker-compose.prod.yml` (MySQL + App)

### 환경 변수 (.env)
```env
DOCKER_ID=your_docker_hub_username
MYSQL_ROOT_PASSWORD=strong_password
MYSQL_DATABASE=boarddb
MYSQL_USER=boarduser
MYSQL_PASSWORD=strong_password
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/boarddb
SPRING_DATASOURCE_USERNAME=boarduser
SPRING_DATASOURCE_PASSWORD=strong_password
```

---

## 📡 API 엔드포인트

### 게시글 API (`/api/posts/kt`)
- `GET /` - 게시글 목록 (페이징, 정렬)
- `GET /{id}` - 게시글 상세 (댓글 포함)
- `GET /search` - 게시글 검색 (제목+내용)
- `POST /` - 게시글 생성
- `PUT /{id}` - 게시글 수정
- `DELETE /{id}` - 게시글 삭제
- `GET /{id}/sync` - 동기 방식 조회 (성능 비교용)
- `GET /{id}/async` - 비동기 방식 조회 (Coroutines)
- `GET /version` - 배포 버전 확인 (CI/CD 테스트용)

### 댓글 API (`/api/comments/kt`)
- `GET /post/{postId}` - 특정 게시글의 댓글 목록
- `POST /` - 댓글 생성
- `PUT /{id}` - 댓글 수정
- `DELETE /{id}` - 댓글 삭제

### 좋아요 API (`/api/post-likes/kt`)
- `POST /` - 좋아요 추가
- `DELETE /{id}` - 좋아요 취소
- `GET /post/{postId}` - 게시글의 좋아요 수

### Actuator (`/actuator`)
- `GET /health` - 헬스체크
- `GET /info` - 애플리케이션 정보

### API 문서
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## 🛠️ 기술 스택

### Backend
- **Language**: Kotlin 2.0.21
- **Framework**: Spring Boot 3.2.0
- **Build**: Gradle (Kotlin DSL)
- **JDK**: 17

### Database
- **개발**: H2 (인메모리)
- **운영**: MySQL 8.0
- **ORM**: Spring Data JPA
- **QueryDSL**: 5.0.0 (타입 안전한 쿼리)

### Kotlin 라이브러리
- **Coroutines**: 1.7.3 (비동기 처리)
- **Jackson Kotlin Module**: JSON 직렬화
- **Kotlin Reflect**: 리플렉션

### 문서화 & 테스트
- **API 문서**: SpringDoc OpenAPI 2.3.0
- **Test**: MockK 1.13.8, SpringMockK 4.0.2

### DevOps
- **컨테이너**: Docker, Docker Compose
- **CI/CD**: GitHub Actions
- **배포**: AWS EC2
- **레지스트리**: Docker Hub
- **모니터링**: Spring Boot Actuator

---

## 🎯 주요 학습 포인트

### 1. Kotlin 기본 문법
- Null 안전성 (`?`, `!!`, `?.`, `?:`)
- 데이터 클래스 (`data class`)
- 확장 함수 (`fun Type.extensionFunc()`)
- 고차 함수와 람다
- Sealed Class와 Enum

### 2. Spring Boot with Kotlin
- Constructor Injection (생성자 주입)
- `@RestController`, `@Service`, `@Repository`
- JPA 엔티티 설정 (open class 이슈)
- QueryDSL with Kotlin

### 3. Coroutines
- `suspend` 함수
- `coroutineScope`와 `async/await`
- 병렬 처리를 통한 성능 개선

### 4. Docker & CI/CD
- Multi-stage 빌드로 이미지 경량화
- GitHub Actions 워크플로우 작성
- SSH를 통한 EC2 자동 배포
- 환경 변수 관리 (`.env`)

### 5. API 설계
- RESTful API 설계
- DTO 패턴
- Swagger/OpenAPI 문서화
- 페이징과 정렬 처리

---

## 🚦 개발 워크플로우

### 로컬 개발
```bash
# 개발 환경 실행 (H2 DB)
./gradlew bootRun --args='--spring.profiles.active=dev'

# 또는 Docker Compose
docker-compose -f docker-compose.dev.yml up
```

### 빌드 & 테스트
```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# JAR 생성 (테스트 제외)
./gradlew bootJar -x test
```

### 운영 배포
```bash
# 1. 코드 수정
# 2. 커밋 & 푸시
git add .
git commit -m "feat: 새로운 기능 추가"
git push origin main

# 3. GitHub Actions가 자동으로:
#    - 빌드
#    - Docker 이미지 생성 및 푸시
#    - EC2 배포
```

### EC2 수동 배포 (필요시)
```bash
# EC2 접속
ssh -i your-key.pem ec2-user@YOUR_EC2_IP

# 최신 이미지 가져오기
cd ~/kotlin-board-example
docker-compose -f docker-compose.prod.yml pull app
docker-compose -f docker-compose.prod.yml up -d app

# 확인
docker ps
docker logs kotlin-board-prod
```

---

## 📝 코드 작성 가이드라인

### Kotlin 코딩 컨벤션
- 변수명: camelCase
- 클래스명: PascalCase
- 상수: UPPER_SNAKE_CASE
- 함수는 동사로 시작
- 불변 변수 우선 (`val` > `var`)

### 에러 처리
- `EntityNotFoundException`: 리소스를 찾을 수 없을 때
- `IllegalArgumentException`: 잘못된 파라미터
- `@ControllerAdvice`로 전역 예외 처리

### API 응답 형식
```kotlin
// 단일 객체
data class PostResponse(
    val id: Long,
    val title: String,
    val content: String
)

// 목록 + 페이징
data class PostListResponse(
    val content: List<PostResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
```

---

## 🔒 보안 고려사항

### 환경 변수 관리
- ❌ `.env` 파일 절대 Git에 커밋 금지
- ✅ `.env.example` 템플릿만 제공
- ✅ GitHub Secrets 사용

### SSH 키 관리
- ❌ `.pem` 파일 절대 Git에 커밋 금지
- ✅ GitHub Secrets에 저장
- ✅ 키 권한 `chmod 400`

### 비밀번호
- ✅ 강력한 비밀번호 (12자 이상)
- ✅ 특수문자, 숫자, 대소문자 조합
- ✅ 정기적 변경

### EC2 보안 그룹
- SSH(22): GitHub Actions IP 또는 VPN만 허용
- HTTP(8080): 필요한 IP만 허용
- MySQL(3306): 내부 통신만 허용

---

## 📚 참고 문서

### 프로젝트 문서
- `CICD_SETUP.md` - CI/CD 설정 상세 가이드
- `DOCKER_CICD.md` - Docker 사용법
- `KOTLIN_GUIDE.md` - Kotlin 학습 자료
- `README.md` - 프로젝트 개요
- `API_EXAMPLES.md` - API 사용 예제
- `QUICK_START.md` - 빠른 시작 가이드
- `TESTING_GUIDE.md` - 테스트 가이드

### 외부 참고
- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [Spring Boot Kotlin 가이드](https://spring.io/guides/tutorials/spring-boot-kotlin/)
- [Coroutines 가이드](https://kotlinlang.org/docs/coroutines-guide.html)
- [QueryDSL](http://querydsl.com/)

---

## 🎓 학습 목표

1. ✅ Java 코드를 Kotlin으로 변환하는 방법 이해
2. ✅ Kotlin의 null 안전성과 간결한 문법 활용
3. ✅ Spring Boot와 Kotlin 통합
4. ✅ Coroutines를 활용한 비동기 처리
5. ✅ QueryDSL로 타입 안전한 쿼리 작성
6. ✅ Docker를 활용한 컨테이너화
7. ✅ GitHub Actions로 CI/CD 파이프라인 구축
8. ✅ AWS EC2 배포 및 운영

---

## 🤝 기여 방법

1. 이슈 생성 또는 기존 이슈 확인
2. 브랜치 생성: `git checkout -b feature/amazing-feature`
3. 커밋: `git commit -m 'feat: Add amazing feature'`
4. 푸시: `git push origin feature/amazing-feature`
5. Pull Request 생성

---

## 📞 문의

- GitHub Issues: https://github.com/dbswhd4932/kotlin-board/issues
- 프로젝트 저장소: https://github.com/dbswhd4932/kotlin-board
