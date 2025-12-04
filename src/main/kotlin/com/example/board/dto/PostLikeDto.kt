package com.example.board.dto

import java.time.LocalDateTime

/**
 * PostLike 관련 DTO (Data Transfer Object)
 *
 * [학습 포인트]
 * 1. Data Class
 *    - 자동으로 equals, hashCode, toString, copy 메서드 생성
 *    - JSON 직렬화/역직렬화에 최적
 *    - Java의 Record와 유사
 *
 * 2. DTO 패턴
 *    - Controller와 비즈니스 로직 분리
 *    - API 응답 형식 명확화
 *    - Entity를 직접 노출하지 않음 (보안, 유연성)
 *
 * 3. Sealed Class vs Data Class
 *    - sealed class: 여러 DTO를 그룹화 (권장)
 *    - data class: 단일 DTO
 */
object PostLikeDto {

    /**
     * 좋아요 추가/취소 응답
     *
     * [사용 예시]
     * POST /api/posts/{postId}/likes
     * Response: { "postId": 1, "userId": 123, "message": "좋아요를 추가했습니다." }
     */
    data class LikeResponse(
        val postId: Long,
        val userId: Long,
        val message: String
    )

    /**
     * 좋아요 개수 응답
     *
     * [사용 예시]
     * GET /api/posts/{postId}/likes/count
     * Response: { "postId": 1, "count": 42 }
     */
    data class LikeCountResponse(
        val postId: Long,
        val count: Long
    )

    /**
     * 좋아요 여부 확인 응답
     *
     * [사용 예시]
     * GET /api/posts/{postId}/likes/check?userId=123
     * Response: { "postId": 1, "userId": 123, "isLiked": true }
     */
    data class LikeCheckResponse(
        val postId: Long,
        val userId: Long,
        val isLiked: Boolean
    )

    /**
     * 좋아요 목록 조회 응답
     *
     * [사용 예시]
     * GET /api/posts/{postId}/likes
     * Response: {
     *   "postId": 1,
     *   "count": 3,
     *   "likes": [
     *     { "id": 1, "postId": 1, "userId": 100, "createdAt": "..." },
     *     { "id": 2, "postId": 1, "userId": 101, "createdAt": "..." }
     *   ]
     * }
     */
    data class LikeListResponse(
        val postId: Long,
        val count: Int,
        val likes: List<LikeInfo>
    )

    /**
     * 개별 좋아요 정보
     *
     * [학습 포인트]
     * - Entity의 모든 필드를 노출하지 않음
     * - 필요한 정보만 선택적으로 제공
     */
    data class LikeInfo(
        val id: Long?,
        val postId: Long,
        val userId: Long,
        val createdAt: LocalDateTime
    )
}
