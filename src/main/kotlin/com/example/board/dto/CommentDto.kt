package com.example.board.dto

import com.example.board.entity.Comment
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * 댓글 DTO (Kotlin 버전)
 *
 * [Java와 비교]
 * - object: Java의 static class와 유사
 * - data class: getter/setter 자동 생성
 * - @field: 어노테이션을 필드에 적용 (중요!)
 * - companion object: static 메서드 대체
 */
object CommentDto {

    /**
     * 댓글 생성 요청 DTO
     *
     * @field:NotBlank - Validation 어노테이션은 @field: 필수!
     */
    data class CreateCommentRequest(
        @field:NotBlank(message = "내용은 필수입니다")
        val content: String,

        @field:NotBlank(message = "작성자는 필수입니다")
        @field:Size(max = 50, message = "작성자는 50자 이하여야 합니다")
        val author: String
    ) {
        // DTO → Entity 변환
        fun toEntity(): Comment {
            return Comment(
                content = content,
                author = author
            )
        }
    }

    /**
     * 댓글 수정 요청 DTO
     */
    data class UpdateCommentRequest(
        @field:NotBlank(message = "내용은 필수입니다")
        val content: String
    )

    /**
     * 댓글 응답 DTO
     */
    data class CommentResponse(
        val id: Long,
        val content: String,
        val author: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        val postId: Long
    ) {
        // companion object 안의 메서드 = Java의 static 메서드
        companion object {
            // Entity → DTO 변환 (Java의 static 메서드)
            fun from(comment: Comment): CommentResponse {
                return CommentResponse(
                    id = comment.id!!,  // !!: null이 아님을 확신
                    content = comment.content,
                    author = comment.author,
                    createdAt = comment.createdAt,
                    updatedAt = comment.updatedAt,
                    postId = comment.post?.id ?: 0L  // ?: Elvis 연산자 (null이면 0L)
                )
            }
        }
    }
}
