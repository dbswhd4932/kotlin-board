package com.example.board.repository

import com.example.board.entity.Comment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 댓글 Repository (Kotlin 버전)
 *
 * [Java와 비교]
 * - interface는 Java와 거의 동일
 * - extends → : (콜론으로 상속)
 * - List<Comment> → List<Comment> (타입만 변경)
 * - @Repository는 생략 가능 (JpaRepository 상속 시 자동 Bean 등록)
 *
 * [Query Method]
 * - findByPostId: post_id로 댓글 조회
 * - findByAuthor: 작성자명으로 댓글 조회
 * - Spring Data JPA가 메서드 이름으로 쿼리 자동 생성
 */
@Repository
interface CommentRepository : JpaRepository<Comment, Long> {

    // 특정 게시글의 모든 댓글 조회
    // SELECT * FROM comments WHERE post_id = ?
    fun findByPostId(postId: Long): List<Comment>

    // 특정 작성자의 모든 댓글 조회
    // SELECT * FROM comments WHERE author = ?
    fun findByAuthor(author: String): List<Comment>
}
