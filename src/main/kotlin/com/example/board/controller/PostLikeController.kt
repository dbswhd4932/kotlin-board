package com.example.board.controller

import com.example.board.dto.PostLikeDto
import com.example.board.service.PostLikeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * PostLike Controller (Kotlin 학습용)
 *
 * [학습 포인트]
 * 1. RESTful API 설계
 *    - POST /api/posts/{postId}/likes : 좋아요 추가
 *    - DELETE /api/posts/{postId}/likes : 좋아요 취소
 *    - GET /api/posts/{postId}/likes : 좋아요 개수 조회
 *    - GET /api/posts/{postId}/likes/check : 좋아요 여부 확인
 *
 * 2. Path Variable vs Request Param
 *    - @PathVariable: URL 경로에 포함 (/posts/{postId})
 *    - @RequestParam: 쿼리 스트링 (?userId=1)
 *
 * 3. ResponseEntity
 *    - ResponseEntity.ok(): 200 OK
 *    - ResponseEntity.status(HttpStatus.CREATED): 201 Created
 *    - ResponseEntity.noContent(): 204 No Content
 *
 * 4. Data Class (Response DTO)
 *    - JSON 응답을 위한 DTO
 *    - data class는 자동으로 equals, hashCode, toString 생성
 */
@RestController
@RequestMapping("/api/posts")
@Tag(name = "좋아요 API", description = "게시글 좋아요 기능 API")
class PostLikeController(
    private val postLikeService: PostLikeService
) {

    @Operation(summary = "좋아요 추가", description = "게시글에 좋아요를 추가합니다")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "좋아요 추가 성공"),
        ApiResponse(responseCode = "409", description = "이미 좋아요를 누른 경우")
    )
    @PostMapping("/{postId}/likes")
    fun addLike(
        @Parameter(description = "게시글 ID", example = "1", required = true)
        @PathVariable postId: Long,
        @Parameter(description = "사용자 ID", example = "1", required = true)
        @RequestParam userId: Long
    ): ResponseEntity<PostLikeDto.LikeResponse> {
        // 1. Service 호출
        val like = postLikeService.addLike(postId, userId)

        // 2. Response DTO 생성
        val response = PostLikeDto.LikeResponse(
            postId = like.postId,
            userId = like.userId,
            message = "좋아요를 추가했습니다."
        )

        // 3. 201 Created 응답
        return ResponseEntity.status(HttpStatus.CREATED).body(response)

    }

    @Operation(summary = "좋아요 취소", description = "게시글의 좋아요를 취소합니다")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "좋아요 취소 성공"),
        ApiResponse(responseCode = "404", description = "좋아요를 찾을 수 없음")
    )
    @DeleteMapping("/{postId}/likes")
    fun removeLike(
        @Parameter(description = "게시글 ID", example = "1")
        @PathVariable postId: Long,
        @Parameter(description = "사용자 ID", example = "1")
        @RequestParam userId: Long
    ): ResponseEntity<Unit> {
        postLikeService.removeLike(postId, userId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "좋아요 개수 조회", description = "특정 게시글의 좋아요 개수를 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{postId}/likes/count")
    fun getLikeCount(
        @Parameter(description = "게시글 ID", example = "1")
        @PathVariable postId: Long
    ): ResponseEntity<PostLikeDto.LikeCountResponse> {
        val count = postLikeService.getLikeCount(postId)
        val response = PostLikeDto.LikeCountResponse(
            postId = postId,
            count = count
        )
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "좋아요 여부 확인", description = "사용자가 특정 게시글에 좋아요를 눌렀는지 확인합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{postId}/likes/check")
    fun checkLikeStatus(
        @Parameter(description = "게시글 ID", example = "1")
        @PathVariable postId: Long,
        @Parameter(description = "사용자 ID", example = "1")
        @RequestParam userId: Long
    ): ResponseEntity<PostLikeDto.LikeCheckResponse> {
        val isLiked = postLikeService.isLikedByUser(postId, userId)
        val response = PostLikeDto.LikeCheckResponse(
            postId = postId,
            userId = userId,
            isLiked = isLiked
        )
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "좋아요 목록 조회", description = "특정 게시글의 모든 좋아요 목록을 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{postId}/likes")
    fun getLikesByPost(
        @Parameter(description = "게시글 ID", example = "1")
        @PathVariable postId: Long
    ): ResponseEntity<PostLikeDto.LikeListResponse> {
        val likes = postLikeService.getLikesByPostId(postId)
        val likeDtos = likes.map { like ->
            PostLikeDto.LikeInfo(like.id, like.postId, like.userId, like.createdAt)
        }
        val response = PostLikeDto.LikeListResponse(
            postId = postId,
            count = likes.size,
            likes = likeDtos
        )
        return ResponseEntity.ok(response)
    }
}

