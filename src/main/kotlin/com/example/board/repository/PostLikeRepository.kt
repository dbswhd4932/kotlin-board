package com.example.board.repository

import com.example.board.entity.PostLike
import org.springframework.data.jpa.repository.JpaRepository

/**
 * PostLike Repository (Kotlin 학습용)
 *
 * [학습 포인트]
 * 1. Interface 선언
 *    - Java: public interface PostLikeRepository extends JpaRepository<PostLike, Long>
 *    - Kotlin: interface PostLikeRepository : JpaRepository<PostLike, Long>
 *
 * 2. JpaRepository 제공 기본 메서드
 *    - save(entity): 저장
 *    - findById(id): 조회
 *    - findAll(): 전체 조회
 *    - deleteById(id): 삭제
 *    - existsById(id): 존재 여부
 *
 * 3. Query Method (메서드 이름으로 쿼리 자동 생성)
 *    - findByPostId: WHERE post_id = ?
 *    - findByUserId: WHERE user_id = ?
 *    - findByPostIdAndUserId: WHERE post_id = ? AND user_id = ?
 *    - existsByPostIdAndUserId: 존재 여부 확인
 *    - deleteByPostIdAndUserId: 삭제
 *    - countByPostId: 개수 세기
 */
interface PostLikeRepository : JpaRepository<PostLike, Long> {

    // 학습 포인트 - Query Method
    // 특정 게시글의 좋아요 찾기
    // 메서드 이름만으로 쿼리 생성: SELECT * FROM post_likes WHERE post_id = ?
    // 반환 타입: PostLike? (nullable - 없을 수 있음)
    fun findByPostIdAndUserId(postId: Long, userId: Long): PostLike?

    // 학습 포인트 - exists 메서드
    // 좋아요가 이미 존재하는지 확인 (중복 방지)
    // 반환 타입: Boolean (true/false)
    fun existsByPostIdAndUserId(postId: Long, userId: Long): Boolean

    // 학습 포인트 - delete 메서드
    // 특정 게시글의 특정 사용자 좋아요 삭제
    // 반환 타입: Int (삭제된 개수)
    // @Transactional 필요 (삭제는 트랜잭션 필요)
    fun deleteByPostIdAndUserId(postId: Long, userId: Long): Int

    // 학습 포인트 - count 메서드
    // 특정 게시글의 좋아요 개수 세기
    // 반환 타입: Long (개수)
    fun countByPostId(postId: Long): Long

    // 학습 포인트 - findAll 변형
    // 특정 게시글의 모든 좋아요 찾기
    // 반환 타입: List<PostLike> (빈 리스트 or 실제 리스트)
    fun findAllByPostId(postId: Long): List<PostLike>

    // 학습 포인트 - findAll 변형 2
    // 특정 사용자가 누른 모든 좋아요 찾기
    fun findAllByUserId(userId: Long): List<PostLike>
}
