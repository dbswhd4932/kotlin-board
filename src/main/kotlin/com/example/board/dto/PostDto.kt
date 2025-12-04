package com.example.board.dto

import com.example.board.entity.Comment
import com.example.board.entity.Post
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * 게시글 DTO (Kotlin 버전)
 *
 * [Java와 비교]
 * - Java 350줄 → Kotlin 150줄 (약 60% 감소!)
 * - data class: getter/setter 자동 생성
 * - @field: Validation 어노테이션은 @field: 필수
 * - companion object: static 메서드 대체
 * - map { }: stream().map().collect() 대체
 */
object PostDto {

    /**
     * 게시글 생성 요청 DTO
     *
     * [Kotlin 포인트]
     * - @field:NotBlank: 필드에 어노테이션 적용
     * - val: 불변 프로퍼티 (요청 DTO는 변경 불필요)
     * - toEntity(): DTO → Entity 변환 메서드
     */
    data class CreatePostRequest(
        @field:NotBlank(message = "제목은 필수입니다")
        @field:Size(max = 200, message = "제목은 200자 이하여야 합니다")
        val title: String,

        @field:NotBlank(message = "내용은 필수입니다")
        val content: String,

        @field:NotBlank(message = "작성자는 필수입니다")
        @field:Size(max = 50, message = "작성자는 50자 이하여야 합니다")
        val author: String
    ) {
        // DTO → Entity 변환
        fun toEntity(): Post {
            return Post(
                title = title,
                content = content,
                author = author
            )
        }
    }

    /**
     * 게시글 수정 요청 DTO
     */
    data class UpdatePostRequest(
        @field:NotBlank(message = "제목은 필수입니다")
        @field:Size(max = 200, message = "제목은 200자 이하여야 합니다")
        val title: String,

        @field:NotBlank(message = "내용은 필수입니다")
        val content: String
    )

    /**
     * 게시글 응답 DTO
     *
     * [Kotlin 포인트]
     * - companion object: Java의 static 메서드
     * - from(): Entity → DTO 변환 팩토리 메서드
     */
    data class PostResponse(
        val id: Long,
        val title: String,
        val content: String,
        val author: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        val commentCount: Int
    ) {
        companion object {
            // Entity → DTO 변환 (Java의 static 메서드)
            fun from(post: Post): PostResponse {
                return PostResponse(
                    id = post.id!!,  // !!: null이 아님을 확신
                    title = post.title,
                    content = post.content,
                    author = post.author,
                    createdAt = post.createdAt,
                    updatedAt = post.updatedAt,
                    commentCount = post.comments.size
                )
            }
        }
    }

    /**
     * 게시글 상세 응답 DTO (댓글 포함)
     *
     * [Kotlin 포인트]
     * - map { }: Java의 stream().map().collect() 대체
     * - it: 람다의 단일 파라미터
     */
    data class PostDetailResponse(
        val id: Long,
        val title: String,
        val content: String,
        val author: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        val comments: List<CommentDto.CommentResponse>,  // CommentDto 참조
        val likeCount: Long // 좋아요 개수 응답 추가
    ) {
        companion object {
            fun from(post: Post): PostDetailResponse {
                return PostDetailResponse(
                    id = post.id!!,
                    title = post.title,
                    content = post.content,
                    author = post.author,
                    createdAt = post.createdAt,
                    updatedAt = post.updatedAt,
                    // stream().map().collect() → map { }
                    comments = post.comments.map { CommentDto.CommentResponse.from(it) },
                    likeCount = 0
                )
            }

            // 새로운 팩토리 메서드 추가 > 병렬 조회한 데이터를 조합하기 위함
            fun of(
                post: Post,
                comments: List<Comment>,
                likeCount: Long
            ): PostDetailResponse {
                return PostDetailResponse(
                    id = post.id!!,
                    title = post.title,
                    content = post.content,
                    author = post.author,
                    createdAt = post.createdAt,
                    updatedAt = post.updatedAt,
                    comments = comments.map { CommentDto.CommentResponse.from(it) },
                    likeCount = likeCount
                )
            }
        }
    }

    data class PageRequest(
        val page: Int = 0,
        val size: Int = 20,
        val sortBy: String = "createdAt",
        val direction: SortDirection = SortDirection.DESCENDING
    )

    enum class SortDirection {
        ASCENDING, DESCENDING
    }

    /**
     * 게시글 목록 응답 DTO (페이징 정보 포함)
     *
     * [사용 예시]
     * val response = PostListResponse(
     *     posts = page.content.map { PostResponse.from(it) },
     *     totalElements = page.totalElements,
     *     ...
     * )
     */
    data class PostListResponse(
        val posts: List<PostResponse>,
        val totalElements: Long,
        val totalPages: Int,
        val currentPage: Int,
        val size: Int
    )
}
