package com.example.board.controller

import com.example.board.dto.CommentDto
import com.example.board.service.CommentService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Java의 CommentController를 Kotlin으로 변환
 *
 * 학습 목표:
 * 1. @RestController, @RequestMapping 어노테이션 사용법 (Java와 동일)
 * 2. constructor injection 패턴 (Kotlin은 생성자에서 바로 프로퍼티 선언 가능)
 * 3. @PathVariable, @RequestBody, @Valid 어노테이션 활용
 * 4. ResponseEntity 사용법 (Kotlin에서도 동일하게 사용)
 *
 * Kotlin 변환 포인트:
 * - Java의 생성자 주입 → Kotlin의 primary constructor로 간결하게 표현
 *   Java: private final CommentService commentService;
 *         public CommentController(CommentService commentService) {
 *             this.commentService = commentService;
 *         }
 *   Kotlin: class CommentController(private val commentService: CommentService)
 *
 * - ResponseEntity 생성
 *   Java: ResponseEntity.ok(response)
 *   Kotlin: ResponseEntity.ok(response) (동일)
 *
 * - HTTP Status 지정
 *   Java: ResponseEntity.status(HttpStatus.CREATED).body(response)
 *   Kotlin: ResponseEntity.status(HttpStatus.CREATED).body(response) (동일)
 */
@RestController
@RequestMapping("/api/kt/posts/{postId}/comments")
@Tag(name = "댓글 API", description = "게시글 댓글 CRUD API")
class CommentController(
    private val commentService: CommentService
) {

    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 모든 댓글을 조회합니다")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공")
    )
    @GetMapping
    fun getComments(
        @Parameter(description = "게시글 ID", example = "1")
        @PathVariable postId: Long
    ): ResponseEntity<List<CommentDto.CommentResponse>> {
        val response = commentService.getCommentsByPostId(postId)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "댓글 생성", description = "게시글에 새로운 댓글을 작성합니다")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "생성 성공"),
        ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    )
    @PostMapping
    fun createComment(
        @Parameter(description = "게시글 ID", example = "1")
        @PathVariable postId: Long,
        @Parameter(description = "댓글 생성 요청", required = true)
        @Valid @RequestBody request: CommentDto.CreateCommentRequest
    ): ResponseEntity<CommentDto.CommentResponse> {
        val response = commentService.createComment(postId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(summary = "댓글 수정", description = "기존 댓글을 수정합니다")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "수정 성공"),
        ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    )
    @PutMapping("/{commentId}")
    fun updateComment(
        @Parameter(description = "게시글 ID", example = "1")
        @PathVariable postId: Long,
        @Parameter(description = "댓글 ID", example = "1")
        @PathVariable commentId: Long,
        @Parameter(description = "댓글 수정 요청", required = true)
        @Valid @RequestBody request: CommentDto.UpdateCommentRequest
    ): ResponseEntity<CommentDto.CommentResponse> {
        val response = commentService.updateComment(commentId, request)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "삭제 성공"),
        ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    )
    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @Parameter(description = "게시글 ID", example = "1")
        @PathVariable postId: Long,
        @Parameter(description = "댓글 ID", example = "1")
        @PathVariable commentId: Long
    ): ResponseEntity<Void> {
        commentService.deleteComment(commentId)
        return ResponseEntity.noContent().build()
    }

}
