# Java to Kotlin 리팩토링 실습 프로젝트

Java 개발자가 직접 Kotlin으로 변환하면서 배우는 게시판 애플리케이션입니다.

## 🎯 프로젝트 목적

**Java로 작성된 게시판 코드를 직접 Kotlin으로 리팩토링하면서 Kotlin 문법을 익히세요!**

## 📚 학습 방법

1. **Java 코드 분석**: 먼저 Java 코드를 읽고 이해하기
2. **Kotlin으로 변환**: CONVERSION_GUIDE.md를 참고하며 직접 변환
3. **실행 및 테스트**: 변환한 코드가 정상 동작하는지 확인
4. **비교 학습**: Java와 Kotlin 코드를 비교하며 차이점 학습

## 🛠 기술 스택

- **언어**: Java 17 (현재) → Kotlin (변환 목표)
- **프레임워크**: Spring Boot 3.2.0, Spring Data JPA
- **데이터베이스**: H2 (인메모리)
- **빌드 도구**: Gradle (Kotlin DSL)

## 📁 현재 프로젝트 구조 (Java)

```
kotlin-board-example/
├── src/
│   ├── main/
│   │   ├── java/                               👈 Java 소스 (변환 대상)
│   │   │   └── com/example/board/
│   │   │       ├── BoardApplication.java
│   │   │       ├── entity/
│   │   │       │   ├── Post.java
│   │   │       │   └── Comment.java
│   │   │       ├── dto/
│   │   │       │   ├── PostDto.java
│   │   │       │   └── CommentDto.java
│   │   │       ├── repository/
│   │   │       │   ├── PostRepository.java
│   │   │       │   └── CommentRepository.java
│   │   │       ├── service/
│   │   │       │   ├── PostService.java
│   │   │       │   └── CommentService.java
│   │   │       └── controller/
│   │   │           ├── PostController.java
│   │   │           ├── CommentController.java
│   │   │           └── GlobalExceptionHandler.java
│   │   ├── kotlin/                             👈 여기에 Kotlin 코드 작성!
│   │   │   └── (비어있음 - 직접 작성할 공간)
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/
└── build.gradle.kts
```

## 🚀 변환 목표 구조 (Kotlin)

```
kotlin-board-example/
├── src/
│   ├── main/
│   │   ├── kotlin/                             👈 Kotlin 코드 (작성 후)
│   │   │   └── com/example/board/
│   │   │       ├── BoardApplication.kt
│   │   │       ├── entity/
│   │   │       │   ├── Post.kt
│   │   │       │   └── Comment.kt
│   │   │       └── ...
│   │   └── resources/
│   │       └── application.yml
```

## Java vs Kotlin 주요 차이점

### 1. 변수 선언

**Java:**
```java
private final String name = "John";  // 불변
private int age = 30;                 // 가변
```

**Kotlin:**
```kotlin
val name = "John"  // 불변 (final)
var age = 30       // 가변
```

### 2. Null Safety

**Java:**
```java
String name = null;  // NPE 위험
if (name != null) {
    System.out.println(name.length());
}
```

**Kotlin:**
```kotlin
val name: String? = null  // nullable 명시
println(name?.length)     // Safe call (?.)
val length = name?.length ?: 0  // Elvis 연산자 (?:)
```

### 3. 데이터 클래스

**Java:**
```java
public class User {
    private Long id;
    private String name;

    // constructor, getter, setter, equals, hashCode, toString 필요
}
```

**Kotlin:**
```kotlin
data class User(
    val id: Long,
    val name: String
)
// equals, hashCode, toString, copy 자동 생성
```

### 4. 생성자 주입

**Java:**
```java
@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }
}
```

**Kotlin:**
```kotlin
@Service
class PostService(
    private val postRepository: PostRepository
)
// 생성자 파라미터에서 바로 프로퍼티 선언
```

### 5. 컬렉션 변환

**Java:**
```java
List<PostResponse> posts = postList.stream()
    .map(PostResponse::from)
    .collect(Collectors.toList());
```

**Kotlin:**
```kotlin
val posts = postList.map { PostResponse.from(it) }
// 더 간결한 표현
```

### 6. 스코프 함수

**Kotlin의 강력한 기능:**

```kotlin
// apply: 객체 초기화
val post = Post().apply {
    title = "제목"
    content = "내용"
}

// let: null 체크 후 실행
post?.let {
    println(it.title)
}

// also: 객체를 사용하고 반환
val savedPost = postRepository.save(post).also {
    logger.info("Saved post: ${it.id}")
}
```

### 7. 확장 함수

**Kotlin의 독특한 기능:**

```kotlin
// String에 새로운 메서드 추가
fun String.isEmail(): Boolean {
    return this.contains("@")
}

val email = "test@example.com"
println(email.isEmail())  // true
```

## API 엔드포인트

### 게시글 API

| Method | URL | 설명 |
|--------|-----|------|
| GET | /api/posts | 게시글 목록 조회 (페이징) |
| GET | /api/posts/{id} | 게시글 상세 조회 |
| GET | /api/posts/search?keyword={keyword} | 게시글 검색 |
| POST | /api/posts | 게시글 생성 |
| PUT | /api/posts/{id} | 게시글 수정 |
| DELETE | /api/posts/{id} | 게시글 삭제 |

### 댓글 API

| Method | URL | 설명 |
|--------|-----|------|
| GET | /api/posts/{postId}/comments | 댓글 목록 조회 |
| POST | /api/posts/{postId}/comments | 댓글 생성 |
| PUT | /api/posts/{postId}/comments/{commentId} | 댓글 수정 |
| DELETE | /api/posts/{postId}/comments/{commentId} | 댓글 삭제 |

## 실행 방법

### 1. 프로젝트 빌드

```bash
cd kotlin-board-example
./gradlew build
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 3. H2 Console 접속

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:boarddb`
- Username: `sa`
- Password: (빈칸)

## API 테스트 예제

### 게시글 생성

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "첫 번째 게시글",
    "content": "게시글 내용입니다.",
    "author": "홍길동"
  }'
```

### 게시글 목록 조회

```bash
curl http://localhost:8080/api/posts?page=0&size=10
```

### 게시글 상세 조회

```bash
curl http://localhost:8080/api/posts/1
```

### 댓글 작성

```bash
curl -X POST http://localhost:8080/api/posts/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "content": "좋은 글이네요!",
    "author": "김철수"
  }'
```

## 📖 학습 문서

### 필수 문서 (순서대로 읽기)

1. **README.md** (현재 문서)
   - 프로젝트 전체 개요 및 실행 방법

2. **CONVERSION_GUIDE.md** ⭐ 가장 중요!
   - Java → Kotlin 변환 단계별 가이드
   - 각 파일별 변환 체크리스트
   - 자주 하는 실수 모음

3. **KOTLIN_GUIDE.md**
   - Java vs Kotlin 문법 상세 비교
   - 스코프 함수, 확장 함수 등 심화 내용

4. **API_EXAMPLES.md**
   - API 테스트 curl 명령어 모음
   - 전체 시나리오 테스트 스크립트

5. **QUICK_START.md**
   - 5분 안에 시작하는 빠른 가이드

## 🎓 학습 포인트

이 프로젝트를 통해 배울 수 있는 Kotlin 핵심 개념:

### 기본 문법
- ✅ `val` vs `var` (불변 vs 가변)
- ✅ Null Safety (`?`, `?.`, `?:`, `!!`)
- ✅ 타입 추론
- ✅ 문자열 보간 (`$name`)

### 객체지향
- ✅ **Data Class**: getter/setter/equals 자동 생성
- ✅ **생성자 프로퍼티**: 생성자에서 바로 필드 선언
- ✅ **Companion Object**: static 메서드 대체

### 함수형 프로그래밍
- ✅ **컬렉션 함수**: `map`, `filter`, `find` 등
- ✅ **스코프 함수**: `let`, `apply`, `also`, `run`
- ✅ **확장 함수**: 기존 클래스에 메서드 추가

### Spring Boot와 통합
- ✅ JPA Entity with data class
- ✅ Repository 인터페이스
- ✅ Service 생성자 주입
- ✅ Controller REST API

## 🚀 시작하기

### 1. 현재 Java 코드 실행해보기

```bash
cd kotlin-board-example

# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

### 2. API 테스트

```bash
# 게시글 생성
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title":"첫 게시글","content":"내용","author":"홍길동"}'

# 게시글 목록 조회
curl http://localhost:8080/api/posts
```

### 3. Kotlin으로 변환 시작

**추천 순서:**

1. **Entity 변환** (Post.java → Post.kt)
   - 가장 간단하고 기본이 되는 클래스
   - data class 사용법 익히기

2. **DTO 변환** (PostDto.java → PostDto.kt)
   - companion object 학습
   - validation 어노테이션 처리

3. **Repository 변환**
   - Optional → nullable 변환
   - 거의 Java와 동일

4. **Service 변환** ⭐ 중요!
   - 생성자 주입 간소화
   - Elvis 연산자 활용
   - 컬렉션 함수 사용

5. **Controller 변환**
   - fun 키워드
   - 파라미터 타입 변환

6. **Application 변환**
   - main 함수 클래스 밖으로

### 4. 빌드 및 테스트

```bash
# Kotlin으로 변환한 후
./gradlew clean build
./gradlew bootRun

# API 테스트로 동작 확인
curl http://localhost:8080/api/posts
```

## 💡 변환 팁

### IntelliJ 자동 변환 활용

1. Java 파일 내용 복사
2. Kotlin 파일 생성 후 붙여넣기
3. IntelliJ가 자동으로 Kotlin 코드로 변환 제안
4. 변환된 코드를 리뷰하고 개선

### 한 파일씩 변환

- 전체를 한번에 변환하지 말고 하나씩
- 각 파일 변환 후 빌드 확인
- 점진적으로 변환하면 오류 찾기 쉬움

### Java와 Kotlin 혼용 가능

- 일부만 Kotlin으로 변환해도 OK
- Java와 Kotlin은 100% 호환
- 원하는 만큼만 변환하며 학습 가능

## 📊 학습 진행도 체크

- [ ] Java 코드 읽고 이해하기
- [ ] CONVERSION_GUIDE.md 읽기
- [ ] Entity 변환 (Post, Comment)
- [ ] DTO 변환
- [ ] Repository 변환
- [ ] Service 변환
- [ ] Controller 변환
- [ ] Application 변환
- [ ] 전체 빌드 성공
- [ ] API 테스트 통과

## 🔗 추가 학습 자료

- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [Kotlin Playground](https://play.kotlinlang.org/) - 온라인 실습
- [Spring Boot with Kotlin](https://spring.io/guides/tutorials/spring-boot-kotlin/)
- [Kotlin for Java Developers (Coursera)](https://www.coursera.org/learn/kotlin-for-java-developers)

## 📝 라이센스

MIT License

---

**Happy Kotlin Learning! 🎉**
