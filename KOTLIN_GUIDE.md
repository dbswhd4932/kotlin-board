# Java 개발자를 위한 Kotlin 가이드

## 목차
1. [기본 문법](#1-기본-문법)
2. [Null Safety](#2-null-safety)
   - [Nullable 타입 (`?`)](#221--nullable-타입-선언)
   - [Safe Call 연산자 (`?.`)](#222--safe-call-연산자)
   - [Elvis 연산자 (`?:`)](#223--elvis-연산자-엘비스-연산자)
   - [Non-null Assertion (`!!`)](#224--non-null-assertion-operator-강제-언래핑)
3. [클래스와 객체](#3-클래스와-객체)
4. [함수형 프로그래밍](#4-함수형-프로그래밍)
   - [람다 표현식](#41-람다-표현식-lambda-expressions)
   - [암시적 파라미터 (`it`)](#412-it---암시적-파라미터)
5. [스코프 함수](#5-스코프-함수)
6. [확장 함수](#6-확장-함수)
7. [어노테이션 Use-Site Targets (`@field`)](#7-어노테이션-use-site-targets)
8. [Spring Boot와 함께 사용하기](#8-spring-boot와-함께-사용하기)
   - [Repository - Optional vs Nullable](#832-optional-vs-nullable---매우-중요)
   - [Service - 문자열 템플릿](#842-문자열-템플릿-string-templates)
   - [Service - JPA Dirty Checking](#843-jpa-dirty-checking-변경-감지)

---

## 1. 기본 문법

### 1.1 변수 선언

**Java:**
```java
final String name = "John";  // 불변
int age = 30;                 // 가변
Long id = null;               // nullable
```

**Kotlin:**
```kotlin
val name = "John"      // 불변 (final) - 타입 추론
var age = 30           // 가변
val id: Long? = null   // nullable 명시
```

**핵심 차이점:**
- `val` = value (불변, Java의 final)
- `var` = variable (가변)
- 타입 추론: 컴파일러가 자동으로 타입 결정
- `?`: nullable 타입 명시적 표현

### 1.2 함수 선언

**Java:**
```java
public String greet(String name) {
    return "Hello, " + name;
}

public void printMessage(String msg) {
    System.out.println(msg);
}
```

**Kotlin:**
```kotlin
// 일반 함수
fun greet(name: String): String {
    return "Hello, $name"  // 문자열 보간
}

// 단일 표현식 함수
fun greet(name: String) = "Hello, $name"

// Unit = Java의 void
fun printMessage(msg: String): Unit {
    println(msg)
}

// Unit 생략 가능
fun printMessage(msg: String) {
    println(msg)
}

// 기본 파라미터
fun greet(name: String = "Guest") = "Hello, $name"
```

### 1.3 조건문

**Java:**
```java
String result;
if (score >= 90) {
    result = "A";
} else if (score >= 80) {
    result = "B";
} else {
    result = "C";
}
```

**Kotlin:**
```kotlin
// if는 표현식 (expression)
val result = if (score >= 90) {
    "A"
} else if (score >= 80) {
    "B"
} else {
    "C"
}

// when (Java의 switch와 유사하지만 더 강력)
val result = when {
    score >= 90 -> "A"
    score >= 80 -> "B"
    score >= 70 -> "C"
    else -> "F"
}

// when with argument
val result = when(score) {
    100 -> "Perfect!"
    in 90..99 -> "A"
    in 80..89 -> "B"
    else -> "C"
}
```

---

## 2. Null Safety

Kotlin의 가장 큰 특징 중 하나는 컴파일 타임에 NPE를 방지한다는 것입니다.

### 2.1 Nullable vs Non-Nullable

**Java:**
```java
String name = null;  // 컴파일 OK, 런타임 NPE 위험
name.length();       // NullPointerException!
```

**Kotlin:**
```kotlin
val name: String = null   // 컴파일 에러!
val name: String? = null  // OK

// Safe Call (?)
val length = name?.length  // null이면 null 반환

// Elvis 연산자 (?:)
val length = name?.length ?: 0  // null이면 0 반환

// Non-null assertion (!!)
val length = name!!.length  // null이면 NPE 발생
```

### 2.2 Null Safety 연산자 상세 설명

#### 2.2.1 `?` - Nullable 타입 선언

```kotlin
// Non-nullable (기본)
val name: String = "John"     // null 할당 불가
name = null                   // 컴파일 에러!

// Nullable
val name: String? = "John"    // null 할당 가능
name = null                   // OK
```

**핵심:**
- 타입 뒤에 `?`를 붙이면 nullable 타입
- nullable 타입은 null을 허용하지만, 직접 메서드 호출 불가

```kotlin
val name: String? = "John"
name.length        // 컴파일 에러! (null일 수 있어서)
name?.length       // OK (Safe Call 사용)
```

#### 2.2.2 `?.` - Safe Call 연산자

```kotlin
val name: String? = null

// Java 방식
if (name != null) {
    println(name.length)
}

// Kotlin Safe Call
println(name?.length)  // null이면 전체가 null
```

**동작 방식:**
- 객체가 null이 아니면 → 메서드/프로퍼티 실행
- 객체가 null이면 → null 반환 (NPE 발생 안함)

**체이닝:**
```kotlin
val city = user?.address?.city  // 중간에 null이면 전체가 null
```

#### 2.2.3 `?:` - Elvis 연산자 (엘비스 연산자)

```kotlin
val name: String? = null

// Java 방식
String result = (name != null) ? name : "Guest";

// Kotlin Elvis 연산자
val result = name ?: "Guest"  // name이 null이면 "Guest" 반환
```

**사용 예시:**

```kotlin
// 기본값 설정
val length = name?.length ?: 0

// 예외 던지기
val post = repository.findById(id)
    ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다")

// early return
fun processUser(user: User?) {
    val validUser = user ?: return
    // validUser는 여기서 non-null
}
```

**이름의 유래:**
```kotlin
?: 를 90도 돌리면 엘비스의 헤어스타일처럼 보임!
```

#### 2.2.4 `!!` - Non-null Assertion Operator (강제 언래핑)

```kotlin
val name: String? = "John"
val length: Int = name!!.length  // "나는 이게 null이 아니라고 확신해!"
```

**동작 방식:**
- nullable 타입을 강제로 non-nullable로 변환
- 만약 null이면 → `NullPointerException` 발생

**예시:**

```kotlin
// JPA 엔티티 예시
@Entity
data class Post(
    @Id @GeneratedValue
    var id: Long? = null  // 저장 전엔 null
)

// DB에서 조회한 후
val post = postRepository.findById(1L).orElseThrow()
val postId: Long = post.id!!  // DB에서 가져왔으니 무조건 id가 있음
```

**⚠️ 주의사항:**

```kotlin
// ❌ 나쁜 예: !!를 남발하면 NPE 위험
val result = user!!.address!!.city!!.name!!

// ✅ 좋은 예: Safe Call과 Elvis 연산자 사용
val result = user?.address?.city?.name ?: "Unknown"
```

**언제 사용해야 하나?**
1. JPA 엔티티의 ID처럼 DB에서 가져온 값 (100% null이 아님을 확신)
2. 테스트 코드
3. 플랫폼 타입과 상호작용 시

**언제 피해야 하나?**
- 대부분의 경우! `!!`는 "code smell"로 간주됨
- Safe Call(`?.`)이나 Elvis(`?:`) 연산자로 대체 가능하면 대체

#### 2.2.5 연산자 조합 사용

```kotlin
// Safe Call + Elvis
val length = name?.length ?: 0

// Safe Call + let + Elvis
val result = repository.findById(id)
    ?.let { PostResponse.from(it) }
    ?: throw IllegalArgumentException("Not found")

// Safe Call 체이닝 + Elvis
val cityName = user?.address?.city?.name ?: "Unknown"

// 조건부 실행
post?.comments?.forEach { println(it.content) }
```

### 2.3 실전 예제

**Java:**
```java
public PostResponse getPost(Long id) {
    Post post = postRepository.findById(id).orElse(null);
    if (post == null) {
        throw new IllegalArgumentException("게시글을 찾을 수 없습니다");
    }
    return PostResponse.from(post);
}
```

**Kotlin:**
```kotlin
fun getPost(id: Long): PostResponse {
    val post = postRepository.findByIdOrNull(id)
        ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다")

    return PostResponse.from(post)
}

// 또는
fun getPost(id: Long): PostResponse {
    return postRepository.findByIdOrNull(id)
        ?.let { PostResponse.from(it) }
        ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다")
}
```

---

## 3. 클래스와 객체

### 3.1 데이터 클래스

**Java:**
```java
public class User {
    private Long id;
    private String name;
    private String email;

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // getter, setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // ... (20줄 이상)

    @Override
    public boolean equals(Object o) { /* ... */ }

    @Override
    public int hashCode() { /* ... */ }

    @Override
    public String toString() { /* ... */ }
}
```

**Kotlin:**
```kotlin
data class User(
    val id: Long,
    val name: String,
    val email: String
)
// equals, hashCode, toString, copy, componentN 자동 생성!

// 사용 예
val user = User(1L, "John", "john@example.com")
val copy = user.copy(name = "Jane")  // copy 메서드
```

### 3.2 생성자

**Java:**
```java
@Service
public class PostService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Autowired  // 또는 생성자에 @RequiredArgsConstructor
    public PostService(PostRepository postRepository,
                      CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }
}
```

**Kotlin:**
```kotlin
@Service
class PostService(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository
) {
    // 생성자 파라미터에서 바로 프로퍼티 선언!
}
```

### 3.3 상속과 오버라이드

**Java:**
```java
public class Animal {
    public void sound() {
        System.out.println("Some sound");
    }
}

public class Dog extends Animal {
    @Override
    public void sound() {
        System.out.println("Bark!");
    }
}
```

**Kotlin:**
```kotlin
// Kotlin의 클래스는 기본적으로 final
// 상속 가능하게 하려면 open 키워드 필요
open class Animal {
    open fun sound() {
        println("Some sound")
    }
}

class Dog : Animal() {
    override fun sound() {
        println("Bark!")
    }
}
```

---

## 4. 함수형 프로그래밍

### 4.1 람다 표현식 (Lambda Expressions)

람다는 익명 함수를 간결하게 표현하는 방법입니다.

#### 4.1.1 기본 문법

**Java:**
```java
// 익명 클래스
button.setOnClickListener(new OnClickListener() {
    @Override
    public void onClick(View v) {
        System.out.println("Clicked");
    }
});

// Java 8+ 람다
button.setOnClickListener(v -> System.out.println("Clicked"));

// 메서드 레퍼런스
list.stream().map(String::toUpperCase);
```

**Kotlin:**
```kotlin
// 람다 표현식
val sum = { x: Int, y: Int -> x + y }
println(sum(1, 2))  // 3

// 타입 추론
val sum: (Int, Int) -> Int = { x, y -> x + y }

// 단일 파라미터는 it으로 접근 가능
val double = { it: Int -> it * 2 }
val double: (Int) -> Int = { it * 2 }

// 파라미터 없는 람다
val greet = { println("Hello") }
greet()
```

#### 4.1.2 `it` - 암시적 파라미터

람다의 파라미터가 하나일 때, `it`으로 자동 참조 가능:

```kotlin
// 명시적 파라미터
listOf(1, 2, 3).map { number -> number * 2 }

// it 사용 (더 간결)
listOf(1, 2, 3).map { it * 2 }

// 복잡한 경우는 명시적 이름 사용 권장
posts.filter { post -> post.author == "John" }  // 가독성 좋음
posts.filter { it.author == "John" }             // 짧지만 it이 뭔지 불명확할 수 있음
```

#### 4.1.3 후행 람다 (Trailing Lambda)

마지막 파라미터가 람다면 괄호 밖으로 뺄 수 있음:

```kotlin
// 일반 형태
repeat(3, { println("Hello") })

// 후행 람다
repeat(3) { println("Hello") }

// 람다가 유일한 파라미터면 괄호 생략
list.forEach({ println(it) })
list.forEach { println(it) }
```

#### 4.1.4 람다에서 return

```kotlin
// 로컬 return (람다만 종료)
fun processItems(items: List<Int>) {
    items.forEach {
        if (it == 0) return@forEach  // 이 반복만 스킵
        println(it)
    }
    println("Done")  // 실행됨
}

// non-local return (함수 전체 종료)
fun processItems(items: List<Int>) {
    items.forEach {
        if (it == 0) return  // 함수 전체 종료!
        println(it)
    }
    println("Done")  // 실행 안됨
}
```

#### 4.1.5 고차 함수 (Higher-Order Functions)

함수를 파라미터로 받거나 반환하는 함수:

```kotlin
// 함수를 파라미터로 받음
fun calculate(x: Int, y: Int, operation: (Int, Int) -> Int): Int {
    return operation(x, y)
}

// 사용
val sum = calculate(5, 3) { a, b -> a + b }        // 8
val product = calculate(5, 3) { a, b -> a * b }    // 15

// 함수를 반환
fun makeMultiplier(factor: Int): (Int) -> Int {
    return { number -> number * factor }
}

val double = makeMultiplier(2)
println(double(5))  // 10
```

### 4.2 컬렉션 변환

**Java:**
```java
List<Post> posts = postRepository.findAll();

// Stream API
List<PostResponse> responses = posts.stream()
    .map(PostResponse::from)
    .collect(Collectors.toList());

List<PostResponse> filtered = posts.stream()
    .filter(post -> post.getAuthor().equals("John"))
    .map(PostResponse::from)
    .collect(Collectors.toList());
```

**Kotlin:**
```kotlin
val posts = postRepository.findAll()

// 간결한 컬렉션 함수
val responses = posts.map { PostResponse.from(it) }

val filtered = posts
    .filter { it.author == "John" }
    .map { PostResponse.from(it) }

// it: 람다의 단일 파라미터 (implicit parameter)
```

### 4.3 주요 컬렉션 함수

```kotlin
val numbers = listOf(1, 2, 3, 4, 5)

// map: 변환
numbers.map { it * 2 }  // [2, 4, 6, 8, 10]

// filter: 필터링
numbers.filter { it > 3 }  // [4, 5]

// find: 첫 번째 매칭 요소
numbers.find { it > 3 }  // 4

// any: 하나라도 만족하는가?
numbers.any { it > 10 }  // false

// all: 모두 만족하는가?
numbers.all { it > 0 }  // true

// groupBy: 그룹화
val posts = listOf(...)
posts.groupBy { it.author }  // Map<String, List<Post>>

// associate: Map 변환
posts.associate { it.id to it.title }  // Map<Long, String>

// flatMap: 중첩 컬렉션 평탄화
posts.flatMap { it.comments }  // List<Comment>
```

---

## 5. 스코프 함수

Kotlin의 독특하고 강력한 기능입니다.

### 5.1 let

- 주로 null 체크 후 실행할 때 사용
- 반환: 람다의 결과

```kotlin
// Java
Post post = postRepository.findById(id).orElse(null);
if (post != null) {
    return PostResponse.from(post);
}

// Kotlin
postRepository.findByIdOrNull(id)?.let {
    return PostResponse.from(it)
}
```

### 5.2 apply

- 객체 초기화할 때 주로 사용
- 반환: 객체 자신

```kotlin
// Java
Post post = new Post();
post.setTitle("제목");
post.setContent("내용");
post.setAuthor("작성자");

// Kotlin
val post = Post().apply {
    title = "제목"
    content = "내용"
    author = "작성자"
}
```

### 5.3 also

- 객체를 사용하고 객체를 반환
- 로깅이나 부가 작업에 유용

```kotlin
val savedPost = postRepository.save(post).also {
    logger.info("Saved post: ${it.id}")
}
```

### 5.4 run

- 객체의 메서드 호출 후 결과 반환

```kotlin
val result = post.run {
    update(title, content)
    PostResponse.from(this)
}
```

### 5.5 with

- 객체를 파라미터로 받음

```kotlin
val response = with(post) {
    PostResponse(
        id = id!!,
        title = title,
        content = content,
        author = author
    )
}
```

---

## 6. 확장 함수

기존 클래스에 새로운 메서드를 추가할 수 있습니다.

```kotlin
// String에 이메일 검증 함수 추가
fun String.isEmail(): Boolean {
    return this.contains("@") && this.contains(".")
}

// 사용
val email = "test@example.com"
if (email.isEmail()) {
    println("Valid email")
}

// Spring Data JPA의 확장 함수 예
// findById(id).orElse(null) -> findByIdOrNull(id)
postRepository.findByIdOrNull(id)
```

---

## 7. 어노테이션 Use-Site Targets

Kotlin에서 Java 어노테이션을 사용할 때, 어노테이션이 적용될 정확한 위치를 지정해야 할 때가 있습니다.

### 7.1 `@field` - 필드에 어노테이션 적용

**문제 상황:**

```kotlin
data class User(
    @NotNull  // 이게 어디에 적용되나요?
    val name: String
)
```

Kotlin의 프로퍼티는 Java에서:
- 필드 (field)
- Getter 메서드
- 생성자 파라미터

총 3곳으로 변환됩니다.

**해결: Use-Site Target 지정**

```kotlin
data class CommentRequest(
    // Validation 어노테이션을 필드에 적용
    @field:NotBlank(message = "내용은 필수입니다")
    val content: String,

    // JPA 어노테이션
    @field:Column(nullable = false)
    val author: String
)
```

### 7.2 Use-Site Targets 종류

```kotlin
class Example {
    @field:Anno      // Java field에 적용
    @get:Anno        // getter에 적용
    @set:Anno        // setter에 적용
    @param:Anno      // 생성자 파라미터에 적용
    @property:Anno   // Kotlin 프로퍼티 자체에 적용
    val name: String
}
```

**실제 예시:**

```kotlin
@Entity
data class Post(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:Column(nullable = false, length = 200)
    @field:NotBlank(message = "제목은 필수입니다")
    var title: String,

    @field:OneToMany(mappedBy = "post", cascade = [CascadeType.ALL])
    val comments: MutableList<Comment> = mutableListOf()
)
```

**왜 필요한가?**

```kotlin
// ❌ @field 없이
@NotBlank
val title: String
// → Kotlin 컴파일러가 어디에 적용할지 모름

// ✅ @field와 함께
@field:NotBlank
val title: String
// → 명확하게 Java field에 적용
```

### 7.3 일반적인 사용 패턴

```kotlin
// JPA Entity
@Entity
data class User(
    @field:Id
    val id: Long,

    @field:Column(unique = true)
    @field:Email
    val email: String
)

// DTO with Validation
data class CreatePostRequest(
    @field:NotBlank(message = "제목은 필수입니다")
    @field:Size(min = 1, max = 200, message = "제목은 1-200자여야 합니다")
    val title: String,

    @field:NotBlank(message = "내용은 필수입니다")
    val content: String
)

// Jackson JSON 직렬화
data class ApiResponse(
    @field:JsonProperty("user_name")
    val userName: String,

    @field:JsonIgnore
    val internalId: Long
)
```

---

## 8. Spring Boot와 함께 사용하기

### 8.1 필수 플러그인

```kotlin
plugins {
    kotlin("plugin.spring")  // @Component 등을 open class로
    kotlin("plugin.jpa")     // @Entity를 open class로
}
```

**왜 필요한가?**
- Kotlin의 클래스는 기본적으로 final
- Spring AOP와 JPA는 프록시 생성을 위해 상속 필요
- 이 플러그인들이 자동으로 open class로 변환

### 8.2 JPA Entity 작성 팁

```kotlin
@Entity
data class Post(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,  // nullable로 선언 (DB가 생성)

    var title: String,     // 변경 가능한 필드는 var

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL])
    val comments: MutableList<Comment> = mutableListOf()
) {
    // equals/hashCode는 id 기반으로 재정의 권장
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Post
        return id != null && id == other.id
    }

    override fun hashCode() = id?.hashCode() ?: 0
}
```

### 8.3 Repository

#### 8.3.1 기본 사용법

```kotlin
interface PostRepository : JpaRepository<Post, Long> {
    // Query Method는 Java와 동일
    fun findByAuthor(author: String): List<Post>

    // Kotlin의 nullable 반환
    fun findByIdOrNull(id: Long): Post?
}
```

#### 8.3.2 Optional vs Nullable - 매우 중요!

**문제 상황:**

```kotlin
// ❌ 이렇게 하면 안 됩니다!
val post = postRepository.findById(id)
    ?: throw IllegalArgumentException("...")  // 컴파일 에러는 없지만 작동 안함!
```

**왜 안 될까요?**

```kotlin
// findById()는 Optional<Post>를 반환
postRepository.findById(id)  // 타입: Optional<Post>

// Optional은 절대 null이 아닙니다!
// 빈 Optional도 객체이므로 Elvis 연산자(?:)가 작동하지 않음
```

**해결 방법:**

```kotlin
// 방법 1: orElseThrow() 사용 (Java 스타일)
val post = postRepository.findById(id)
    .orElseThrow { IllegalArgumentException("게시글을 찾을 수 없습니다. id: $id") }

// 방법 2: orElse(null) + Elvis (코틀린 스타일)
val post = postRepository.findById(id).orElse(null)
    ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다. id: $id")

// 방법 3: findByIdOrNull() 확장 함수 (가장 코틀린다움!) ✅ 추천
val post = postRepository.findByIdOrNull(id)
    ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다. id: $id")
```

**비교표:**

| 메서드 | 반환 타입 | Elvis 연산자 | 추천도 |
|--------|-----------|--------------|--------|
| `findById(id)` | `Optional<Post>` | ❌ 사용 불가 | Java 호환 |
| `findById(id).orElse(null)` | `Post?` | ✅ 사용 가능 | 🤔 |
| `findByIdOrNull(id)` | `Post?` | ✅ 사용 가능 | ⭐ 추천 |

**핵심 요약:**

```kotlin
// Java Optional과 Kotlin nullable은 다릅니다!
Optional.empty()  // null이 아님! 빈 Optional 객체
null              // null

// 따라서
Optional.empty() ?: "default"  // ❌ 작동 안함 (Optional은 null이 아니므로)
null ?: "default"              // ✅ "default" 반환
```

### 8.4 Service

#### 8.4.1 기본 구조

```kotlin
@Service
@Transactional(readOnly = true)
class PostService(
    private val postRepository: PostRepository
) {
    fun getPost(id: Long): PostResponse {
        val post = postRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다")

        return PostResponse.from(post)
    }

    @Transactional
    fun createPost(request: CreatePostRequest): PostResponse {
        val post = request.toEntity()
        val savedPost = postRepository.save(post)
        return PostResponse.from(savedPost)
    }
}
```

#### 8.4.2 문자열 템플릿 (String Templates)

**Java 방식 (연결 연산자):**
```java
throw new IllegalArgumentException("게시글을 찾을 수 없습니다. id: " + id);
```

**Kotlin 방식 (문자열 템플릿):**
```kotlin
// ❌ Java 스타일 (작동은 하지만 권장하지 않음)
throw IllegalArgumentException("게시글을 찾을 수 없습니다. id: " + id)

// ✅ Kotlin 스타일 (문자열 템플릿 사용)
throw IllegalArgumentException("게시글을 찾을 수 없습니다. id: $id")

// 표현식도 가능
throw IllegalArgumentException("게시글을 찾을 수 없습니다. id: ${id}, user: ${user.name}")
```

**문자열 템플릿 규칙:**

```kotlin
val name = "John"
val age = 30

// 단순 변수: $변수명
println("Name: $name")  // "Name: John"

// 표현식: ${표현식}
println("Age: ${age + 1}")  // "Age: 31"
println("Name length: ${name.length}")  // "Name length: 4"

// 프로퍼티 접근
println("User: ${user.name}, Email: ${user.email}")
```

#### 8.4.3 JPA Dirty Checking (변경 감지)

**중요: Kotlin도 Java와 동일하게 Dirty Checking이 작동합니다!**

**Java:**
```java
@Transactional
public PostResponse updatePost(Long id, UpdatePostRequest request) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

    // Dirty Checking을 통한 업데이트
    post.update(request.getTitle(), request.getContent());

    // ✅ save() 호출 불필요! 트랜잭션 종료 시 자동 UPDATE
    return PostResponse.from(post);
}
```

**Kotlin (동일한 동작):**
```kotlin
@Transactional
fun updatePost(id: Long, request: UpdatePostRequest): PostResponse {
    val post = postRepository.findByIdOrNull(id)
        ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다. id: $id")

    // Dirty Checking을 통한 업데이트
    post.update(request.title, request.content)

    // ✅ save() 호출 불필요! 트랜잭션 종료 시 자동 UPDATE
    return PostResponse.from(post)
}
```

**Dirty Checking 작동 조건:**

1. ✅ `@Transactional` 어노테이션 존재
2. ✅ Repository에서 조회한 **영속 상태** 엔티티
3. ✅ 엔티티의 필드 변경 (`var` 프로퍼티)
4. ✅ 트랜잭션 커밋 시점에 자동 UPDATE

**잘못된 예:**

```kotlin
@Transactional
fun updatePost(id: Long, request: UpdatePostRequest): PostResponse {
    val post = postRepository.findByIdOrNull(id)
        ?: throw IllegalArgumentException("...")

    post.update(request.title, request.content)

    // ❌ 불필요한 save() 호출 (중복 UPDATE 쿼리 가능성)
    val savedPost = postRepository.save(post)

    return PostResponse.from(savedPost)
}
```

**핵심:**
- JPA의 Dirty Checking은 **언어와 무관**
- Kotlin이든 Java든 **동일한 JPA 메커니즘** 사용
- `@Transactional` + 영속 엔티티 수정 = 자동 UPDATE

### 8.5 Controller

```kotlin
@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postService: PostService
) {
    @GetMapping
    fun getPosts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<PostListResponse> {
        val response = postService.getPosts(
            PageRequest.of(page, size)
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping
    fun createPost(
        @Valid @RequestBody request: CreatePostRequest
    ): ResponseEntity<PostResponse> {
        val response = postService.createPost(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
```

---

## 실전 팁

### 1. Java 코드와 호환

Kotlin은 Java와 100% 호환됩니다. 같은 프로젝트에서 Java와 Kotlin을 섞어 사용 가능합니다.

### 2. IntelliJ 자동 변환

IntelliJ IDEA는 Java 코드를 Kotlin으로 자동 변환해줍니다:
- Java 파일 복사 → Kotlin 파일에 붙여넣기
- Code → Convert Java File to Kotlin File

### 3. 점진적 마이그레이션

기존 Java 프로젝트를 Kotlin으로 전환할 때:
1. 새로운 클래스는 Kotlin으로 작성
2. 기존 Java 클래스는 그대로 유지
3. 필요할 때만 Java → Kotlin 변환

### 4. 학습 순서 추천

1. 기본 문법 (변수, 함수, 클래스)
2. Null Safety
3. 데이터 클래스와 프로퍼티
4. 컬렉션 함수
5. 스코프 함수
6. 확장 함수
7. 코루틴 (비동기 프로그래밍)

---

## 추가 학습 자료

- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [Kotlin Playground](https://play.kotlinlang.org/) - 브라우저에서 실습
- [Kotlin Koans](https://play.kotlinlang.org/koans) - 인터랙티브 학습
- [Spring Boot with Kotlin](https://spring.io/guides/tutorials/spring-boot-kotlin/)
