package com.example.board.service

import com.example.board.dto.CommentDto
import com.example.board.repository.CommentRepository
import com.example.board.repository.PostRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Java의 CommentService를 Kotlin으로 변환
 *
 * 학습 목표:
 * 1. @Service, @Transactional 어노테이션 사용법
 * 2. Repository 주입 및 사용
 * 3. Optional 대신 Kotlin의 nullable 타입 활용
 * 4. Stream API 대신 Kotlin의 Collection 확장 함수 사용
 * 5. Elvis 연산자(?:)를 활용한 예외 처리
 *
 * Kotlin 변환 포인트:
 * 1. Optional 처리
 *    Java: postRepository.findById(postId)
 *          .orElseThrow(() -> new IllegalArgumentException("..."))
 *    Kotlin: postRepository.findById(postId)
 *            ?: throw IllegalArgumentException("...")
 *
 * 2. Stream API → Collection 함수
 *    Java: list.stream()
 *          .map(CommentResponse::from)
 *          .collect(Collectors.toList())
 *    Kotlin: list.map { CommentResponse.from(it) }
 *    또는: list.map(CommentResponse::from)
 *
 * 3. Null 체크
 *    Java: if (comment.getPost() != null) { ... }
 *    Kotlin: comment.post?.let { ... }
 *
 * 4. 생성자 주입
 *    Java: private final CommentRepository commentRepository;
 *          public CommentService(CommentRepository commentRepository, ...) {
 *              this.commentRepository = commentRepository;
 *          }
 *    Kotlin: class CommentService(
 *              private val commentRepository: CommentRepository,
 *              private val postRepository: PostRepository
 *            )
 */
@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository
) {

    /**
     * 구현 힌트:
     * 1. commentRepository.findByPostId(postId) 호출
     * 2. 결과를 map을 사용하여 CommentResponse로 변환
     * 3. Java의 .stream().map().collect() 대신 Kotlin의 .map() 사용
     */
    @Transactional(readOnly = true)
    fun getCommentsByPostId(postId: Long): List<CommentDto.CommentResponse> {
        return commentRepository.findByPostId(postId).map {
            CommentDto.CommentResponse.from(it)
        }
    }

    /**
     * 구현 힌트:
     * 1. postRepository.findById(postId)로 게시글 조회
     * 2. Elvis 연산자(?:)를 사용하여 없으면 예외 발생
     * 3. request.toEntity()로 엔티티 생성
     * 4. comment.post = post로 연관관계 설정
     * 5. commentRepository.save(comment)로 저장
     * 6. 양방향 연관관계 설정: post.addComment(savedComment)
     * 7. CommentResponse.from(savedComment) 반환
     */
    @Transactional
    fun createComment(
        postId: Long,
        request: CommentDto.CreateCommentRequest
    ): CommentDto.CommentResponse {
        val post = postRepository.findById(postId).orElse(null)
            ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다. id: $postId")

        val comment = request.toEntity().apply { this.post = post }

        val savedComment = commentRepository.save(comment)

        post.addComment(savedComment)
        return CommentDto.CommentResponse.from(savedComment)
    }


    /**
     * 구현 힌트:
     * 1. commentRepository.findById(commentId)로 댓글 조회
     * 2. Elvis 연산자로 없으면 예외 발생
     * 3. comment.update(request.content)로 업데이트 (dirty checking)
     * 4. CommentResponse.from(comment) 반환
     */
    @Transactional
    fun updateComment(
        commentId: Long,
        request: CommentDto.UpdateCommentRequest
    ): CommentDto.CommentResponse {
        val comment = commentRepository.findById(commentId).orElse(null)
            ?: throw IllegalArgumentException("댓글을 찾을 수 없습니다. id: $commentId")

        // 변수 자체를 재할당 하는것이 아니기 떄문에 val 사용
        comment.update(request.content)

        return CommentDto.CommentResponse.from(comment)
    }

    /**
     * 구현 힌트:
     * 1. commentRepository.findById(commentId)로 댓글 조회
     * 2. Elvis 연산자로 없으면 예외 발생
     * 3. comment.post?.removeComment(comment)로 양방향 연관관계 제거 (?.let 사용)
     * 4. commentRepository.delete(comment)로 삭제
     */
    @Transactional
    fun deleteComment(commentId: Long) {
        val comment = commentRepository.findById(commentId).orElse(null)
            ?: throw IllegalArgumentException("댓글을 찾을 수 없습니다. id: $commentId")

        comment.post?.removeComment(comment)
        commentRepository.delete(comment)
    }
}
