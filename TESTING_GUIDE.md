# Kotlin TDD 가이드

## 목차
1. [테스트 구조](#테스트-구조)
2. [테스트 레이어별 설명](#테스트-레이어별-설명)
3. [Kotlin 테스트 문법](#kotlin-테스트-문법)
4. [테스트 실행](#테스트-실행)
5. [MockK 사용법](#mockk-사용법)

---

## 테스트 구조

```
src/test/kotlin/com/example/board/
├── repository/
│   └── PostRepositoryKtTest.kt      # Repository 레이어 테스트
├── service/
│   └── PostServiceKtTest.kt         # Service 레이어 테스트 (단위)
├── controller/
│   └── PostControllerKtTest.kt      # Controller 레이어 테스트
└── integration/
    └── PostIntegrationTest.kt       # 통합 테스트
```

---

## 테스트 레이어별 설명

### 1. Repository 테스트 (`@DataJpaTest`)

**목적:** JPA Repository의 쿼리 메서드 검증

**특징:**
- JPA 관련 컴포넌트만 로드 (경량)
- 실제 H2 DB 사용
- 각 테스트 후 트랜잭션 자동 롤백

**예시:**
```kotlin
@DataJpaTest
class PostRepositoryKtTest {
    @Autowired
    private lateinit var postRepositoryKt: PostRepositoryKt

    @Test
    fun findByAuthor() {
        // Given
        val post = PostKt(title = "제목", content = "내용", author = "홍길동")
        postRepositoryKt.save(post)

        // When
        val posts = postRepositoryKt.findByAuthor("홍길동")

        // Then
        assertThat(posts).hasSize(1)
    }
}
```

### 2. Service 테스트 (단위 테스트 with MockK)

**목적:** 비즈니스 로직 검증

**특징:**
- Repository를 Mock으로 대체
- 빠른 실행 속도
- 비즈니스 로직에만 집중

**예시:**
```kotlin
class PostServiceKtTest {
    private lateinit var postServiceKt: PostServiceKt
    private lateinit var postRepositoryKt: PostRepositoryKt

    @BeforeEach
    fun setUp() {
        postRepositoryKt = mockk<PostRepositoryKt>()
        postServiceKt = PostServiceKt(postRepositoryKt)
    }

    @Test
    fun getPost() {
        // Given
        val post = PostKt(id = 1L, title = "제목", content = "내용", author = "작성자")
        every { postRepositoryKt.findByIdWithComments(1L) } returns post

        // When
        val result = postServiceKt.getPost(1L)

        // Then
        assertThat(result.title).isEqualTo("제목")
        verify(exactly = 1) { postRepositoryKt.findByIdWithComments(1L) }
    }
}
```

### 3. Controller 테스트 (`@WebMvcTest`)

**목적:** HTTP 요청/응답 검증

**특징:**
- Controller 레이어만 로드
- MockMvc 사용
- Service는 Mock으로 대체

**예시:**
```kotlin
@WebMvcTest(PostControllerKt::class)
class PostControllerKtTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var postServiceKt: PostServiceKt

    @Test
    fun getPosts() {
        // Given
        val response = PostDtoKt.PostListResponse(...)
        every { postServiceKt.getPosts(any()) } returns response

        // When & Then
        mockMvc.perform(get("/api/posts/kt"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.posts").isArray)
    }
}
```

### 4. 통합 테스트 (`@SpringBootTest`)

**목적:** 전체 플로우 검증

**특징:**
- 전체 애플리케이션 컨텍스트 로드
- 실제 DB 사용
- 모든 레이어 통합 테스트

**예시:**
```kotlin
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun fullCrudFlow() {
        // 생성 → 조회 → 수정 → 삭제 전체 플로우 테스트
    }
}
```

---

## Kotlin 테스트 문법

### 1. 변수 선언

```kotlin
// lateinit: 나중에 초기화 (테스트에서 자주 사용)
@Autowired
private lateinit var mockMvc: MockMvc

// val vs var
val immutableValue = "변경 불가"
var mutableValue = "변경 가능"
```

### 2. 테스트 구조 (Given-When-Then)

```kotlin
@Test
fun testExample() {
    // Given: 테스트 준비
    val post = PostKt(title = "제목", content = "내용", author = "작성자")

    // When: 실행
    val result = postService.createPost(request)

    // Then: 검증
    assertThat(result.title).isEqualTo("제목")
}
```

### 3. AssertJ (Kotlin)

```kotlin
// 기본 검증
assertThat(result).isNotNull()
assertThat(result.id).isEqualTo(1L)
assertThat(result.title).isEqualTo("제목")

// 컬렉션 검증
assertThat(posts).hasSize(10)
assertThat(posts).isEmpty()
assertThat(posts[0].title).contains("키워드")

// 예외 검증
assertThatThrownBy { service.getPost(999L) }
    .isInstanceOf(IllegalArgumentException::class.java)
    .hasMessageContaining("찾을 수 없습니다")
```

---

## MockK 사용법

### 1. Mock 생성

```kotlin
// Mock 객체 생성
val repository = mockk<PostRepositoryKt>()

// Relaxed Mock (모든 메서드가 기본값 반환)
val repository = mockk<PostRepositoryKt>(relaxed = true)
```

### 2. Stub 정의 (every)

```kotlin
// 기본 Stub
every { repository.findById(1L) } returns Optional.of(post)

// nullable 반환
every { repository.findByIdWithComments(1L) } returns post  // PostKt?

// 예외 던지기
every { repository.findById(999L) } throws IllegalArgumentException("Not found")

// 어떤 인자든 허용
every { repository.findById(any()) } returns Optional.of(post)
```

### 3. 인자 캡처 (Slot)

```kotlin
// Slot으로 인자 캡처
val postSlot = slot<PostKt>()
every { repository.save(capture(postSlot)) } returns savedPost

// 캡처된 값 검증
assertThat(postSlot.captured.title).isEqualTo("제목")
```

### 4. 검증 (verify)

```kotlin
// 정확히 1번 호출
verify(exactly = 1) { repository.findById(1L) }

// 한 번도 호출되지 않음
verify(exactly = 0) { repository.deleteById(any()) }

// 최소 1번 호출
verify(atLeast = 1) { repository.save(any()) }
```

### 5. 확장 함수 Mock

```kotlin
// Kotlin 확장 함수 Mock (findByIdOrNull)
mockkStatic("org.springframework.data.repository.CrudRepositoryExtensionsKt")
every { repository.findByIdOrNull(1L) } returns post

// 사용 후 정리
unmockkStatic("org.springframework.data.repository.CrudRepositoryExtensionsKt")
```

### 6. Unit 반환 (just Runs)

```kotlin
// void/Unit 메서드
every { repository.deleteById(1L) } just Runs
```

---

## 테스트 실행

### Gradle 명령어

```bash
# 전체 테스트 실행
./gradlew test

# 특정 클래스만 실행
./gradlew test --tests PostRepositoryKtTest

# 특정 메서드만 실행
./gradlew test --tests PostRepositoryKtTest.findByAuthor

# 테스트 결과 HTML 리포트
./gradlew test
# build/reports/tests/test/index.html 확인
```

### IntelliJ에서 실행

1. 테스트 클래스에서 `Ctrl + Shift + R` (Mac: `Cmd + Shift + R`)
2. 테스트 메서드 옆 녹색 화살표 클릭
3. `Run with Coverage` - 코드 커버리지 확인

---

## 테스트 Best Practices

### 1. 테스트 이름 규칙

```kotlin
// ✅ 좋은 예: 동작_상황_예상결과
@Test
fun createPost_WithValidRequest_ReturnsCreatedPost()

// ✅ 한글 DisplayName 사용
@Test
@DisplayName("유효한 요청으로 게시글을 생성할 수 있다")
fun createPost()
```

### 2. Given-When-Then 패턴

```kotlin
@Test
fun example() {
    // Given: 테스트 데이터 준비
    val request = CreatePostRequest(...)

    // When: 테스트 실행
    val result = service.createPost(request)

    // Then: 결과 검증
    assertThat(result.id).isNotNull()
}
```

### 3. 독립적인 테스트

```kotlin
// ❌ 나쁜 예: 테스트 간 의존성
@Test
fun test1() { savedPost = repository.save(...) }

@Test
fun test2() { repository.findById(savedPost.id) }  // test1에 의존

// ✅ 좋은 예: @BeforeEach 사용
@BeforeEach
fun setUp() {
    savedPost = repository.save(...)
}
```

### 4. 테스트 데이터 빌더

```kotlin
// 테스트 데이터 생성 함수
fun createPost(
    title: String = "기본 제목",
    content: String = "기본 내용",
    author: String = "기본 작성자"
) = PostKt(title = title, content = content, author = author)

@Test
fun example() {
    val post = createPost(title = "특별한 제목")
}
```

---

## 테스트 커버리지 목표

| 레이어 | 목표 커버리지 |
|--------|---------------|
| Repository | 80%+ |
| Service | 90%+ |
| Controller | 85%+ |
| Integration | 주요 플로우 |

---

## 추가 학습 자료

- [MockK 공식 문서](https://mockk.io/)
- [Kotlin Testing](https://kotlinlang.org/docs/jvm-test-using-junit.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [AssertJ](https://assertj.github.io/doc/)
