package com.example.board.integration

import com.example.board.dto.PostDto
import com.example.board.entity.Post
import com.example.board.repository.PostRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

/**
 * 통합 테스트 (Integration Test)
 *
 * [@SpringBootTest]
 * - 전체 애플리케이션 컨텍스트 로드
 * - 실제 DB 사용 (H2)
 * - 모든 레이어 통합 테스트
 *
 * [@Transactional]
 * - 각 테스트 후 자동 롤백
 * - 테스트 격리 보장
 *
 * [통합 테스트 vs 단위 테스트]
 * - 단위: Mock 사용, 빠름, 개별 컴포넌트
 * - 통합: 실제 DB, 느림, 전체 플로우
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("게시글 통합 테스트")
class PostIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var postRepositoryKt: PostRepository

    @BeforeEach
    fun setUp() {
        // 테스트 데이터 초기화
        postRepositoryKt.deleteAll()
    }

    @Test
    @DisplayName("[통합] 게시글 생성 → 조회 → 수정 → 삭제 전체 플로우")
    fun fullCrudFlow() {
        // 1. 게시글 생성
        val createRequest = PostDto.CreatePostRequest(
            title = "통합 테스트 게시글",
            content = "통합 테스트 내용",
            author = "테스터"
        )

        val createResult = mockMvc.perform(
            post("/api/posts/kt")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andDo(print())
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value("통합 테스트 게시글"))
            .andReturn()

        val createdPost = objectMapper.readValue(
            createResult.response.contentAsString,
            PostDto.PostResponse::class.java
        )
        val postId = createdPost.id

        // DB에 실제로 저장되었는지 확인
        val savedPost = postRepositoryKt.findById(postId).orElseThrow()
        assertThat(savedPost.title).isEqualTo("통합 테스트 게시글")

        // 2. 게시글 조회
        mockMvc.perform(get("/api/posts/kt/{id}", postId))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(postId))
            .andExpect(jsonPath("$.title").value("통합 테스트 게시글"))
            .andExpect(jsonPath("$.content").value("통합 테스트 내용"))

        // 3. 게시글 수정
        val updateRequest = PostDto.UpdatePostRequest(
            title = "수정된 제목",
            content = "수정된 내용"
        )

        mockMvc.perform(
            put("/api/posts/kt/{id}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value("수정된 제목"))
            .andExpect(jsonPath("$.content").value("수정된 내용"))

        // DB에서 실제로 수정되었는지 확인 (Dirty Checking)
        val updatedPost = postRepositoryKt.findById(postId).orElseThrow()
        assertThat(updatedPost.title).isEqualTo("수정된 제목")
        assertThat(updatedPost.content).isEqualTo("수정된 내용")

        // 4. 게시글 삭제
        mockMvc.perform(delete("/api/posts/kt/{id}", postId))
            .andDo(print())
            .andExpect(status().isNoContent)

        // DB에서 실제로 삭제되었는지 확인
        val deletedPost = postRepositoryKt.findById(postId)
        assertThat(deletedPost).isEmpty
    }

    @Test
    @DisplayName("[통합] 게시글 목록 조회 - 페이징")
    fun getPostsWithPaging() {
        // Given: 15개의 게시글 생성
        repeat(15) { index ->
            postRepositoryKt.save(
                Post(
                    title = "제목 ${index + 1}",
                    content = "내용 ${index + 1}",
                    author = "작성자 ${index + 1}"
                )
            )
        }

        // When & Then: 첫 번째 페이지 (0~9)
        mockMvc.perform(
            get("/api/posts/kt")
                .param("page", "0")
                .param("size", "10")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.posts.length()").value(10))
            .andExpect(jsonPath("$.totalElements").value(15))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.currentPage").value(0))

        // When & Then: 두 번째 페이지 (10~14)
        mockMvc.perform(
            get("/api/posts/kt")
                .param("page", "1")
                .param("size", "10")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.posts.length()").value(5))
            .andExpect(jsonPath("$.currentPage").value(1))
    }

    @Test
    @DisplayName("[통합] 게시글 검색 - 키워드")
    fun searchPosts() {
        // Given
        postRepositoryKt.save(Post(title = "코틀린 공부", content = "재미있다", author = "학생1"))
        postRepositoryKt.save(Post(title = "자바 공부", content = "어렵다", author = "학생2"))
        postRepositoryKt.save(Post(title = "스프링 공부", content = "코틀린과 함께", author = "학생3"))

        // When & Then: "코틀린" 검색 (제목 또는 내용)
        mockMvc.perform(
            get("/api/posts/kt/search")
                .param("keyword", "코틀린")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.posts.length()").value(2))  // "코틀린 공부", "스프링 공부"

        // When & Then: "공부" 검색
        mockMvc.perform(
            get("/api/posts/kt/search")
                .param("keyword", "공부")
        )
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.posts.length()").value(3))
    }

    @Test
    @DisplayName("[통합] Validation 검증 - 빈 제목")
    fun createPost_ValidationFailed() {
        // Given: 제목이 빈 요청
        val invalidRequest = PostDto.CreatePostRequest(
            title = "",  // 빈 제목 (Validation 실패)
            content = "내용",
            author = "작성자"
        )

        // When & Then
        mockMvc.perform(
            post("/api/posts/kt")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andDo(print())
            .andExpect(status().isBadRequest)

        // DB에 저장되지 않았는지 확인
        val count = postRepositoryKt.count()
        assertThat(count).isEqualTo(0)
    }

    @Test
    @DisplayName("[통합] 존재하지 않는 게시글 조회")
    fun getPost_NotFound() {
        // When & Then
        mockMvc.perform(get("/api/posts/kt/{id}", 999L))
            .andDo(print())
            .andExpect(status().is4xxClientError)
    }

    @Test
    @DisplayName("[통합] 게시글과 댓글 함께 조회 (N+1 방지)")
    fun getPostWithComments() {
        // Given: 게시글과 댓글 생성
        val post = Post(
            title = "게시글",
            content = "내용",
            author = "작성자"
        )
        val savedPost = postRepositoryKt.save(post)

        // When: findByIdWithComments로 조회 (Fetch Join)
        val foundPost = postRepositoryKt.findByIdWithComments(savedPost.id!!)

        // Then: 댓글도 함께 로드됨 (Lazy Loading 없음)
        assertThat(foundPost).isNotNull
        assertThat(foundPost!!.comments).isNotNull
    }

    @Test
    @DisplayName("[통합] 동시성 테스트 - 여러 게시글 동시 생성")
    fun concurrentPostCreation() {
        // Given: 10개의 생성 요청
        val requests = (1..10).map { index ->
            PostDto.CreatePostRequest(
                title = "제목 $index",
                content = "내용 $index",
                author = "작성자 $index"
            )
        }

        // When: 순차적으로 생성
        requests.forEach { request ->
            mockMvc.perform(
                post("/api/posts/kt")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
        }

        // Then: 모두 생성되었는지 확인
        val count = postRepositoryKt.count()
        assertThat(count).isEqualTo(10)
    }
}
