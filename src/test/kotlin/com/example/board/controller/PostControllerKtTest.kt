package com.example.board.controller

import com.example.board.dto.PostDto
import com.example.board.service.PostService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

/**
 * PostController 테스트
 *
 * [@WebMvcTest]
 * - Controller 레이어만 테스트 (경량)
 * - MockMvc 자동 설정
 * - Service는 Mock으로 대체
 *
 * [Kotlin 테스트 포인트]
 * - @MockkBean: Spring + MockK 통합
 * - mockMvc.perform { } - DSL 스타일
 * - ObjectMapper - JSON 직렬화
 */
@WebMvcTest(PostController::class)
@DisplayName("PostController 컨트롤러 테스트")
class PostControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var postServiceKt: PostService

    @Test
    @DisplayName("GET /api/posts/kt - 게시글 목록 조회")
    fun getPosts() {
        // Given
        val posts = listOf(
            PostDto.PostResponse(
                id = 1L,
                title = "제목1",
                content = "내용1",
                author = "작성자1",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                commentCount = 0
            ),
            PostDto.PostResponse(
                id = 2L,
                title = "제목2",
                content = "내용2",
                author = "작성자2",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                commentCount = 1
            )
        )

        val response = PostDto.PostListResponse(
            posts = posts,
            totalElements = 2L,
            totalPages = 1,
            currentPage = 0,
            size = 10
        )

        every { postServiceKt.getPosts(any()) } returns response

        // When & Then
        mockMvc.perform(
            get("/api/posts/kt")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "createdAt")
                .param("direction", "DESC")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.posts").isArray)
            .andExpect(jsonPath("$.posts.length()").value(2))
            .andExpect(jsonPath("$.posts[0].title").value("제목1"))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.currentPage").value(0))

        verify(exactly = 1) { postServiceKt.getPosts(any()) }
    }

    @Test
    @DisplayName("GET /api/posts/kt/{id} - 게시글 상세 조회")
    fun getPost() {
        // Given
        val postId = 1L
        val response = PostDto.PostDetailResponse(
            id = postId,
            title = "테스트 제목",
            content = "테스트 내용",
            author = "작성자",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            comments = emptyList(),
            likeCount = 0,
        )

        every { postServiceKt.getPost(postId) } returns response

        // When & Then
        mockMvc.perform(get("/api/posts/kt/{id}", postId))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(postId))
            .andExpect(jsonPath("$.title").value("테스트 제목"))
            .andExpect(jsonPath("$.content").value("테스트 내용"))
            .andExpect(jsonPath("$.author").value("작성자"))
            .andExpect(jsonPath("$.comments").isArray)

        verify(exactly = 1) { postServiceKt.getPost(postId) }
    }

    @Test
    @DisplayName("GET /api/posts/kt/{id} - 존재하지 않는 게시글 조회 시 404")
    fun getPost_NotFound() {
        // Given
        val postId = 999L

        every { postServiceKt.getPost(postId) } throws IllegalArgumentException("게시글을 찾을 수 없습니다. id: $postId")

        // When & Then
        mockMvc.perform(get("/api/posts/kt/{id}", postId))
            .andDo(print())
            .andExpect(status().is4xxClientError)

        verify(exactly = 1) { postServiceKt.getPost(postId) }
    }

    @Test
    @DisplayName("GET /api/posts/kt/search - 키워드로 게시글 검색")
    fun searchPost() {
        // Given
        val keyword = "테스트"
        val posts = listOf(
            PostDto.PostResponse(
                id = 1L,
                title = "테스트 제목",
                content = "내용",
                author = "작성자",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                commentCount = 0
            )
        )

        val response = PostDto.PostListResponse(
            posts = posts,
            totalElements = 1L,
            totalPages = 1,
            currentPage = 0,
            size = 10
        )

        every { postServiceKt.searchPost(keyword, any()) } returns response

        // When & Then
        mockMvc.perform(
            get("/api/posts/kt/search")
                .param("keyword", keyword)
                .param("page", "0")
                .param("size", "10")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.posts.length()").value(1))
            .andExpect(jsonPath("$.posts[0].title").value("테스트 제목"))

        verify(exactly = 1) { postServiceKt.searchPost(keyword, any()) }
    }

    @Test
    @DisplayName("POST /api/posts/kt - 게시글 생성")
    fun createPost() {
        // Given
        val request = PostDto.CreatePostRequest(
            title = "새 게시글",
            content = "새 내용",
            author = "작성자"
        )

        val response = PostDto.PostResponse(
            id = 1L,
            title = request.title,
            content = request.content,
            author = request.author,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            commentCount = 0
        )

        every { postServiceKt.createPost(any()) } returns response

        // When & Then
        mockMvc.perform(
            post("/api/posts/kt")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isCreated)  // 201 Created
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.title").value("새 게시글"))
            .andExpect(jsonPath("$.content").value("새 내용"))

        verify(exactly = 1) { postServiceKt.createPost(any()) }
    }

    @Test
    @DisplayName("POST /api/posts/kt - Validation 실패 시 400")
    fun createPost_ValidationFailed() {
        // Given: 제목이 빈 요청 (Validation 실패)
        val invalidRequest = """
            {
                "title": "",
                "content": "내용",
                "author": "작성자"
            }
        """.trimIndent()

        // When & Then
        mockMvc.perform(
            post("/api/posts/kt")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
        )
            .andDo(print())
            .andExpect(status().isBadRequest)

        // Service는 호출되지 않아야 함
        verify(exactly = 0) { postServiceKt.createPost(any()) }
    }

    @Test
    @DisplayName("PUT /api/posts/kt/{id} - 게시글 수정")
    fun updatePost() {
        // Given
        val postId = 1L
        val request = PostDto.UpdatePostRequest(
            title = "수정된 제목",
            content = "수정된 내용"
        )

        val response = PostDto.PostResponse(
            id = postId,
            title = request.title,
            content = request.content,
            author = "작성자",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            commentCount = 0
        )

        every { postServiceKt.updatePost(postId, any()) } returns response

        // When & Then
        mockMvc.perform(
            put("/api/posts/kt/{id}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(postId))
            .andExpect(jsonPath("$.title").value("수정된 제목"))
            .andExpect(jsonPath("$.content").value("수정된 내용"))

        verify(exactly = 1) { postServiceKt.updatePost(postId, any()) }
    }

    @Test
    @DisplayName("DELETE /api/posts/kt/{id} - 게시글 삭제")
    fun deletePost() {
        // Given
        val postId = 1L

        every { postServiceKt.deletePost(postId) } returns Unit

        // When & Then
        mockMvc.perform(delete("/api/posts/kt/{id}", postId))
            .andDo(print())
            .andExpect(status().isNoContent)  // 204 No Content

        verify(exactly = 1) { postServiceKt.deletePost(postId) }
    }

    @Test
    @DisplayName("DELETE /api/posts/kt/{id} - 존재하지 않는 게시글 삭제 시 404")
    fun deletePost_NotFound() {
        // Given
        val postId = 999L

        every { postServiceKt.deletePost(postId) } throws IllegalArgumentException("게시글을 찾을 수 없습니다. id: $postId")

        // When & Then
        mockMvc.perform(delete("/api/posts/kt/{id}", postId))
            .andDo(print())
            .andExpect(status().is4xxClientError)

        verify(exactly = 1) { postServiceKt.deletePost(postId) }
    }
}
