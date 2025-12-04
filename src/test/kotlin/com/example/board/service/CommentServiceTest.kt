package com.example.board.service

import com.example.board.dto.CommentDto
import com.example.board.entity.Comment
import com.example.board.entity.Post
import com.example.board.repository.CommentRepository
import com.example.board.repository.PostRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime
import java.util.*

/**
 * CommentService 단위 테스트
 *
 * [학습 목표]
 * 1. 연관관계가 있는 엔티티 테스트 (Post-Comment)
 * 2. 양방향 연관관계 검증
 * 3. 복잡한 비즈니스 로직 테스트
 */
@DisplayName("CommentService 단위 테스트")
class CommentServiceTest {

    private lateinit var commentRepository: CommentRepository
    private lateinit var postRepository: PostRepository
    private lateinit var commentService: CommentService

    private lateinit var testPost: Post
    private lateinit var testComment: Comment
    private lateinit var createRequest: CommentDto.CreateCommentRequest
    private lateinit var updateRequest: CommentDto.UpdateCommentRequest

    @BeforeEach
    fun setup() {
        // Mock 객체 생성
        commentRepository = mockk()
        postRepository = mockk()
        commentService = CommentService(commentRepository, postRepository)

        // 테스트 데이터 초기화
        testPost = Post(
            id = 1L,
            title = "테스트 게시글",
            content = "게시글 내용",
            author = "게시글 작성자",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        testComment = Comment(
            id = 1L,
            content = "테스트 댓글",
            author = "댓글 작성자",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            post = testPost
        )

        createRequest = CommentDto.CreateCommentRequest(
            content = "새 댓글",
            author = "작성자"
        )

        updateRequest = CommentDto.UpdateCommentRequest(
            content = "수정된 댓글"
        )
    }

    // ========== 댓글 생성 테스트 ==========

    @Test
    @DisplayName("TODO: 댓글 생성 성공 - 게시글에 댓글이 추가되어야 함")
    fun createComment_Success() {
        // Given: 존재하는 게시글
        every { postRepository.findByIdOrNull(1L) } returns testPost
        every { commentRepository.save(any()) } returns testComment

        // When: 댓글 생성
        val result = commentService.createComment(1L, createRequest)

        // Then:
        // TODO: 1. 결과가 null이 아닌지 확인
        assertNotNull(result)
        // TODO: 2. content가 testComment.content와 같은지 확인
        assertEquals(testComment.content, result.content)
        // TODO: 3. commentRepository.save()가 호출되었는지 검증
        verify(exactly = 1) { commentRepository.save(any()) }
        // TODO: 4. 양방향 연관관계가 설정되었는지 확인 (testPost.comments에 댓글 추가됨)
        assertEquals(1, testPost.comments.size)
    }

    @Test
    @DisplayName("TODO: 댓글 생성 실패 - 존재하지 않는 게시글에 댓글 생성 시 예외 발생")
    fun createComment_PostNotFound() {
        // Given: 존재하지 않는 게시글
        every { postRepository.findByIdOrNull(999L) } returns null

        // When & Then: IllegalArgumentException 발생
        assertThrows<IllegalArgumentException> {
            commentService.createComment(999L, createRequest)
        }
    }

    // ========== 게시글별 댓글 조회 테스트 ==========

    @Test
    @DisplayName("게시글의 댓글 목록 조회 성공")
    fun getCommentsByPostId_Success() {
        // Given: 게시글에 속한 댓글 목록
        val comments = listOf(testComment)
        every { commentRepository.findByPostId(1L) } returns comments

        // When: 댓글 목록 조회
        val result = commentService.getCommentsByPostId(1L)

        // Then:
        // 1. 결과가 비어있지 않은지 확인
        assertTrue(result.isNotEmpty())
        // 2. 첫 번째 댓글의 content가 "테스트 댓글"인지 확인
        assertEquals("테스트 댓글", result[0].content)
        // 3. 댓글 개수가 1개인지 확인
        assertEquals(1, result.size)
    }

    @Test
    @DisplayName("댓글이 없는 게시글 조회 - 빈 리스트 반환")
    fun getCommentsByPostId_Empty() {
        // Given: 댓글이 없는 게시글
        every { commentRepository.findByPostId(1L) } returns emptyList()

        // When: 댓글 목록 조회
        val result = commentService.getCommentsByPostId(1L)

        // Then: 결과가 빈 리스트인지 확인
        assertTrue(result.isEmpty())
        assertEquals(0, result.size)
    }

    // ========== 댓글 수정 테스트 ==========

    @Test
    @DisplayName("댓글 수정 성공 - 댓글 내용이 변경되어야 함")
    fun updateComment_Success() {
        // Given: 존재하는 댓글
        every { commentRepository.findByIdOrNull(1L) } returns testComment

        // When: 댓글 수정
        val result = commentService.updateComment(1L, updateRequest)

        // Then:
        // 1. 수정된 content가 "수정된 댓글"인지 확인
        assertEquals("수정된 댓글", result.content)
        // 2. testComment.update() 메서드가 호출되어 내용이 변경되었는지 확인
        assertEquals("수정된 댓글", testComment.content)
    }

    @Test
    @DisplayName("댓글 수정 실패 - 존재하지 않는 댓글 수정 시 예외 발생")
    fun updateComment_NotFound() {
        // Given: 존재하지 않는 댓글
        every { commentRepository.findByIdOrNull(999L) } returns null

        // When & Then: IllegalArgumentException 발생
        assertThrows<IllegalArgumentException> {
            commentService.updateComment(999L, updateRequest)
        }
    }

    // ========== 댓글 삭제 테스트 ==========

    @Test
    @DisplayName("댓글 삭제 성공 - 댓글이 삭제되고 양방향 연관관계도 해제되어야 함")
    fun deleteComment_Success() {
        // Given: 존재하는 댓글
        testPost.addComment(testComment)  // 먼저 댓글 추가
        every { commentRepository.findByIdOrNull(1L) } returns testComment
        every { commentRepository.delete(testComment) } returns Unit

        // When: 댓글 삭제
        commentService.deleteComment(1L)

        // Then:
        // 1. commentRepository.delete()가 호출되었는지 검증
        verify(exactly = 1) { commentRepository.delete(testComment) }
        // 2. 양방향 연관관계가 해제되었는지 확인 (testPost.comments에서 제거됨)
        assertEquals(0, testPost.comments.size)
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 존재하지 않는 댓글 삭제 시 예외 발생")
    fun deleteComment_NotFound() {
        // Given: 존재하지 않는 댓글
        every { commentRepository.findByIdOrNull(999L) } returns null

        // When & Then: IllegalArgumentException 발생
        assertThrows<IllegalArgumentException> {
            commentService.deleteComment(999L)
        }
    }

    // ========== 추가 테스트 케이스 ==========

    @Test
    @DisplayName("여러 댓글 생성 - 한 게시글에 여러 댓글이 순서대로 추가되어야 함")
    fun createMultipleComments_Success() {
        // Given: 게시글과 여러 댓글 생성 요청
        val comment1 = Comment(id = 1L, content = "첫 번째 댓글", author = "작성자1", post = testPost)
        val comment2 = Comment(id = 2L, content = "두 번째 댓글", author = "작성자2", post = testPost)

        // postRepository와 commentRepository에 대한 mock 설정
        every { postRepository.findByIdOrNull(1L) } returns testPost
        every { commentRepository.save(any()) } returnsMany listOf(comment1, comment2)

        // When: 두 개의 댓글 생성
        val result1 = commentService.createComment(1L, createRequest)
        val result2 = commentService.createComment(1L, createRequest)

        // Then:
        // 1. 두 댓글 모두 정상 생성되었는지 확인
        assertNotNull(result1)
        assertNotNull(result2)
        // 2. testPost.comments의 크기가 2인지 확인 (양방향 연관관계)
        assertEquals(2, testPost.comments.size)
    }

    @Test
    @DisplayName("댓글이 없는 게시글의 댓글 삭제 시도")
    fun deleteComment_NoPost() {
        // Given: post가 null인 댓글
        val orphanComment = Comment(
            id = 1L,
            content = "고아 댓글",
            author = "작성자",
            post = null
        )

        every { commentRepository.findByIdOrNull(1L) } returns orphanComment
        every { commentRepository.delete(orphanComment) } returns Unit

        // When: 댓글 삭제 (post가 null이어도 삭제는 가능해야 함)
        commentService.deleteComment(1L)

        // Then: 삭제는 성공하지만 removeComment는 호출되지 않음
        verify(exactly = 1) { commentRepository.delete(orphanComment) }
    }
}
