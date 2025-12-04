# Java to Kotlin 변환 학습 프로젝트

## 프로젝트 목적
Java 코드를 Kotlin으로 변환하는 학습

## 구조
- `src/main/java/` - Java 원본 코드
- `src/main/kotlin/` - Kotlin 변환 대상 코드

## 변환 체크리스트

### Entity
- [x] Post
- [x] Comment

### DTO
- [x] PostDto
- [x] CommentDto

### Repository
- [x] PostRepository (기본)
- [x] CommentRepository
- [ ] PostRepositoryCustom (인터페이스)
- [ ] PostRepositoryImpl (QueryDSL 구현)

### Service
- [x] PostService (기본 구현 있음)
- [ ] CommentService

### Controller
- [x] PostController (기본 구현 있음)
- [ ] CommentController
- [ ] GlobalExceptionHandler

## 주요 변환 포인트

### Optional → nullable
```kotlin
// Java: repository.findById(id).orElseThrow(...)
// Kotlin: repository.findById(id) ?: throw ...
```

### Stream → Collection 함수
```kotlin
// Java: list.stream().map(...).collect(...)
// Kotlin: list.map { ... }
```

### null 체크
```kotlin
// Java: if (obj != null) { ... }
// Kotlin: obj?.let { ... }
```
