# Quick Start Guide

## 5분 안에 시작하기

### 1. 애플리케이션 실행

```bash
cd kotlin-board-example
./gradlew bootRun
```

애플리케이션이 시작되면 http://localhost:8080 에서 접속 가능합니다.

### 2. 첫 번째 게시글 작성

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "안녕하세요!",
    "content": "첫 번째 게시글입니다.",
    "author": "홍길동"
  }'
```

### 3. 게시글 확인

```bash
curl http://localhost:8080/api/posts
```

## Java 개발자를 위한 Kotlin 핵심 개념

### 1. 변수 선언
```kotlin
val name = "John"   // final (불변)
var age = 30        // 가변
```

### 2. Null Safety
```kotlin
val name: String? = null     // nullable
val length = name?.length    // Safe call
val length = name?.length ?: 0  // Elvis 연산자
```

### 3. 데이터 클래스
```kotlin
data class User(val id: Long, val name: String)
// equals, hashCode, toString 자동 생성!
```

### 4. 생성자 주입
```kotlin
@Service
class PostService(
    private val postRepository: PostRepository
)
// Java의 @RequiredArgsConstructor 불필요!
```

### 5. 컬렉션 변환
```kotlin
val posts = postRepository.findAll()
val responses = posts.map { PostResponse.from(it) }
// Java의 stream().map().collect() 불필요!
```

## 프로젝트 구조

```
kotlin-board-example/
├── src/main/kotlin/com/example/board/
│   ├── BoardApplication.kt           # 메인
│   ├── entity/
│   │   ├── Post.kt                   # 게시글 Entity
│   │   └── Comment.kt                # 댓글 Entity
│   ├── dto/                          # 요청/응답 DTO
│   ├── repository/                   # JPA Repository
│   ├── service/                      # 비즈니스 로직
│   └── controller/                   # REST API
└── src/main/resources/
    └── application.yml               # 설정
```

## 학습 자료

- **README.md**: 프로젝트 전체 개요
- **KOTLIN_GUIDE.md**: Java vs Kotlin 상세 비교
- **API_EXAMPLES.md**: API 테스트 예제

## 주요 파일 살펴보기

### 1. Entity (Post.kt)
```kotlin
@Entity
data class Post(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var title: String,
    var content: String,
    var author: String
)
```

**Java와 비교:**
- 생성자, getter, setter 자동 생성
- 훨씬 간결한 코드

### 2. Service (PostService.kt)
```kotlin
@Service
class PostService(private val postRepository: PostRepository) {
    fun getPost(id: Long): PostResponse {
        val post = postRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다")
        return PostResponse.from(post)
    }
}
```

**핵심 포인트:**
- `?:`: null이면 예외 발생 (Elvis 연산자)
- 생성자에서 바로 의존성 주입

### 3. Controller (PostController.kt)
```kotlin
@RestController
@RequestMapping("/api/posts")
class PostController(private val postService: PostService) {
    @GetMapping
    fun getPosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<PostListResponse> {
        val response = postService.getPosts(PageRequest.of(page, size))
        return ResponseEntity.ok(response)
    }
}
```

## 다음 단계

1. **코드 읽기**: 각 파일의 주석을 읽으며 Kotlin 문법 이해
2. **API 테스트**: API_EXAMPLES.md의 curl 명령어로 테스트
3. **코드 수정**: 직접 기능을 추가하거나 수정해보기
4. **심화 학습**: KOTLIN_GUIDE.md로 깊이 있는 학습

## 추가 기능 아이디어

- 페이지네이션 개선
- 회원 인증/인가 (Spring Security)
- 파일 업로드
- 좋아요 기능
- 게시글 조회수
- 태그 기능
- RESTful API 문서화 (Swagger/OpenAPI)

## 도움이 필요하면?

- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [Spring Boot with Kotlin](https://spring.io/guides/tutorials/spring-boot-kotlin/)
- [Kotlin Playground](https://play.kotlinlang.org/) - 온라인 실습
