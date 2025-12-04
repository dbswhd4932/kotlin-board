package com.example.board.service

import com.example.board.dto.PostDto
import com.example.board.entity.Post
import com.example.board.repository.CommentRepository
import com.example.board.repository.PostLikeRepository
import com.example.board.repository.PostRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.util.Assert.notNull
import java.time.LocalDateTime
import java.util.*

/**
 * PostService 단위 테스트
 *
 * [학습 목표]
 * 1. MockK를 사용한 Kotlin 스타일 Mocking
 * 2. Given-When-Then 패턴으로 테스트 작성
 * 3. 각 메서드별 정상 케이스와 예외 케이스 테스트
 * 4. verify를 통한 메서드 호출 검증
 *
 * [MockK 주요 기능]
 * - mockk<T>(): Mock 객체 생성
 * - every { ... } returns ...: 메서드 동작 정의
 * - verify { ... }: 메서드 호출 검증
 * - slot(): 파라미터 캡처
 */
@DisplayName("PostService 단위 테스트")
class PostServiceTest {

    // Mock 객체 선언
    private lateinit var postRepository: PostRepository
    private lateinit var postLikeRepository : PostLikeRepository
    private lateinit var postService: PostService

    // 테스트용 데이터
    private lateinit var testPost: Post
    private lateinit var createRequest: PostDto.CreatePostRequest
    private lateinit var updateRequest: PostDto.UpdatePostRequest

    @BeforeEach
    fun setup() {
        // Mock 객체 생성
        postRepository = mockk()
        postService = PostService(postRepository, postLikeRepository)

        // 테스트 데이터 초기화
        testPost = Post(
            id = 1L,
            title = "테스트 제목",
            content = "테스트 내용",
            author = "테스터",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        createRequest = PostDto.CreatePostRequest(
            title = "새 게시글",
            content = "새 내용",
            author = "작성자"
        )

        updateRequest = PostDto.UpdatePostRequest(
            title = "수정된 제목",
            content = "수정된 내용"
        )
    }

    // ========== 게시글 생성 테스트 ==========

    @Test
    @DisplayName("게시글 생성 성공 - 정상적인 요청으로 게시글이 생성되어야 함")
    fun createPost_Success() {
        // Given: postRepository.save()가 호출되면 testPost 반환하도록 설정
        every { postRepository.save(any()) } returns testPost

        // When: 게시글 생성 요청
        val result = postService.createPost(createRequest)

        // Then:
        assertNotNull(result)
        assertEquals(result.title, testPost.title)
        verify(exactly = 1) { postRepository.save(any()) }

    }

    // ========== 게시글 단건 조회 테스트 ==========

    @Test
    @DisplayName("게시글 조회 성공 - ID로 게시글을 정상 조회해야 함")
    fun getPost_Success() {
        // Given: 존재하는 게시글 ID
        every { postRepository.findByIdWithComments(1L) } returns testPost

        // When: 게시글 조회
        val result = postService.getPost(1L)

        // Then:
        assertEquals(1L, result.id)
        assertEquals("테스트 제목", result.title)
    }

    @Test
    @DisplayName("게시글 조회 실패 - 존재하지 않는 ID로 조회 시 예외 발생")
    fun getPost_NotFound() {
        // Given: 존재하지 않는 게시글 ID
        every { postRepository.findByIdWithComments(999L) } returns null

        // When & Then: IllegalArgumentException 발생해야 함
        assertThrows<IllegalArgumentException> {
            postService.getPost(999L)
        }
    }

    // ========== 게시글 목록 조회 테스트 ==========

    @Test
    @DisplayName("게시글 목록 조회 성공 - 페이징된 게시글 목록을 반환해야 함")
    fun getAllPosts_Success() {
        // Given: 페이징 요청과 게시글 목록
        val pageable = PageRequest.of(0, 10)
        val posts = listOf(testPost)
        val page = PageImpl(posts, pageable, 1)

        every { postRepository.findAll(pageable) } returns page

        // When: 게시글 목록 조회
        val result = postService.getPosts(pageable)

        // Then:
        assertTrue(result.posts.isNotEmpty())
        assertEquals("테스트 제목", result.posts[0].title)
        assertEquals(1, result.totalElements)
    }

    // ========== 게시글 수정 테스트 ==========

    @Test
    @DisplayName("게시글 수정 성공 - 기존 게시글이 수정되어야 함")
    fun updatePost_Success() {
        // Given: 존재하는 게시글
        every { postRepository.findByIdOrNull(1L) } returns testPost

        // When: 게시글 수정
        val result = postService.updatePost(1L, updateRequest)

        // Then:
        assertEquals("수정된 제목", result.title)
        assertEquals("수정된 내용", result.content)
        verify(exactly = 1) { postRepository.findByIdOrNull(1L) }

    }

    @Test
    @DisplayName("게시글 수정 실패 - 존재하지 않는 게시글 수정 시 예외 발생")
    fun updatePost_NotFound() {
        // Given: 존재하지 않는 게시글
        every { postRepository.findByIdOrNull(999L) } returns null

        // When & Then: 예외 발생
        assertThrows<IllegalArgumentException> {
            postService.updatePost(999L, updateRequest)
        }

    }

    // ========== 게시글 삭제 테스트 ==========

    @Test
    @DisplayName("게시글 삭제 성공 - 게시글이 삭제되어야 함")
    fun deletePost_Success() {
        // Given: 존재하는 게시글
        every { postRepository.existsById(1L) } returns true
        every { postRepository.deleteById(1L) } returns Unit

        // When: 게시글 삭제
        postService.deletePost(1L)

        // Then: deleteById 메서드가 호출되었는지 검증
        verify(exactly = 1) { postRepository.deleteById(1L) }
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 존재하지 않는 게시글 삭제 시 예외 발생")
    fun deletePost_NotFound() {
        // Given: 존재하지 않는 게시글
        every { postRepository.existsById(999L) } returns false

        // When & Then: 예외 발생
        assertThrows<IllegalArgumentException> {
            postService.deletePost(999L)
        }
    }
}
