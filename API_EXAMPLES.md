# API 사용 예제

이 문서는 게시판 API를 실제로 테스트할 수 있는 curl 명령어 모음입니다.

## 사전 준비

애플리케이션을 실행합니다:
```bash
cd kotlin-board-example
./gradlew bootRun
```

## 게시글 API

### 1. 게시글 생성

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Kotlin과 Spring Boot로 게시판 만들기",
    "content": "Kotlin은 간결하고 안전한 언어입니다. Spring Boot와 함께 사용하면 생산성이 매우 높아집니다.",
    "author": "홍길동"
  }'
```

**응답 예시:**
```json
{
  "id": 1,
  "title": "Kotlin과 Spring Boot로 게시판 만들기",
  "content": "Kotlin은 간결하고 안전한 언어입니다...",
  "author": "홍길동",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00",
  "commentCount": 0
}
```

### 2. 게시글 목록 조회 (페이징)

```bash
# 기본 조회 (첫 페이지, 10개)
curl http://localhost:8080/api/posts

# 두 번째 페이지, 5개씩, 제목으로 정렬
curl "http://localhost:8080/api/posts?page=1&size=5&sortBy=title&direction=ASC"
```

**응답 예시:**
```json
{
  "posts": [
    {
      "id": 1,
      "title": "첫 번째 게시글",
      "content": "내용...",
      "author": "홍길동",
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00",
      "commentCount": 3
    }
  ],
  "totalElements": 100,
  "totalPages": 10,
  "currentPage": 0,
  "size": 10
}
```

### 3. 게시글 상세 조회

```bash
curl http://localhost:8080/api/posts/1
```

**응답 예시:**
```json
{
  "id": 1,
  "title": "첫 번째 게시글",
  "content": "상세 내용...",
  "author": "홍길동",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00",
  "comments": [
    {
      "id": 1,
      "content": "좋은 글입니다!",
      "author": "김철수",
      "createdAt": "2024-01-01T10:05:00",
      "updatedAt": "2024-01-01T10:05:00",
      "postId": 1
    }
  ]
}
```

### 4. 게시글 검색

```bash
curl "http://localhost:8080/api/posts/search?keyword=Kotlin&page=0&size=10"
```

### 5. 게시글 수정

```bash
curl -X PUT http://localhost:8080/api/posts/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "수정된 제목",
    "content": "수정된 내용입니다."
  }'
```

### 6. 게시글 삭제

```bash
curl -X DELETE http://localhost:8080/api/posts/1
```

## 댓글 API

### 1. 댓글 목록 조회

```bash
curl http://localhost:8080/api/posts/1/comments
```

### 2. 댓글 작성

```bash
curl -X POST http://localhost:8080/api/posts/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "content": "정말 유익한 글이네요. 감사합니다!",
    "author": "김철수"
  }'
```

### 3. 댓글 수정

```bash
curl -X PUT http://localhost:8080/api/posts/1/comments/1 \
  -H "Content-Type: application/json" \
  -d '{
    "content": "수정된 댓글 내용입니다."
  }'
```

### 4. 댓글 삭제

```bash
curl -X DELETE http://localhost:8080/api/posts/1/comments/1
```

## 전체 시나리오 테스트

다음은 전체 흐름을 테스트하는 스크립트입니다:

```bash
#!/bin/bash

echo "=== 게시판 API 테스트 ==="

echo -e "\n1. 게시글 생성..."
POST1=$(curl -s -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Kotlin 학습 가이드",
    "content": "Kotlin의 기본 문법과 특징을 알아봅니다.",
    "author": "홍길동"
  }')
echo $POST1 | jq .

echo -e "\n2. 두 번째 게시글 생성..."
POST2=$(curl -s -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Spring Boot와 JPA",
    "content": "Spring Boot에서 JPA를 사용하는 방법",
    "author": "김철수"
  }')
echo $POST2 | jq .

echo -e "\n3. 게시글 목록 조회..."
curl -s http://localhost:8080/api/posts | jq .

echo -e "\n4. 첫 번째 게시글 상세 조회..."
curl -s http://localhost:8080/api/posts/1 | jq .

echo -e "\n5. 댓글 작성..."
COMMENT=$(curl -s -X POST http://localhost:8080/api/posts/1/comments \
  -H "Content-Type: application/json" \
  -d '{
    "content": "좋은 글 감사합니다!",
    "author": "이영희"
  }')
echo $COMMENT | jq .

echo -e "\n6. 게시글 검색 (Kotlin)..."
curl -s "http://localhost:8080/api/posts/search?keyword=Kotlin" | jq .

echo -e "\n7. 게시글 수정..."
curl -s -X PUT http://localhost:8080/api/posts/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Kotlin 완전 정복",
    "content": "Kotlin의 모든 것을 알아봅니다."
  }' | jq .

echo -e "\n=== 테스트 완료 ==="
```

**사용법:**
```bash
chmod +x test-api.sh
./test-api.sh
```

## Validation 오류 테스트

### 잘못된 요청 (제목 누락)

```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{
    "content": "내용만 있음",
    "author": "홍길동"
  }'
```

**에러 응답:**
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "입력값 검증에 실패했습니다",
  "errors": {
    "title": "제목은 필수입니다"
  },
  "timestamp": "2024-01-01T10:00:00"
}
```

### 존재하지 않는 게시글 조회

```bash
curl http://localhost:8080/api/posts/999
```

**에러 응답:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "게시글을 찾을 수 없습니다. id: 999",
  "errors": {},
  "timestamp": "2024-01-01T10:00:00"
}
```

## H2 Console

데이터베이스를 직접 확인하려면 H2 Console을 사용하세요:

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:boarddb`
- Username: `sa`
- Password: (빈칸)

**유용한 SQL 쿼리:**

```sql
-- 모든 게시글 조회
SELECT * FROM posts;

-- 모든 댓글 조회
SELECT * FROM comments;

-- 게시글과 댓글 조인
SELECT p.title, c.content, c.author
FROM posts p
LEFT JOIN comments c ON p.id = c.post_id;

-- 게시글별 댓글 수
SELECT p.id, p.title, COUNT(c.id) as comment_count
FROM posts p
LEFT JOIN comments c ON p.id = c.post_id
GROUP BY p.id, p.title;
```
