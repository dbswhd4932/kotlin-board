package com.example.board.repository

import com.example.board.entity.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 게시글 Repository (Kotlin 버전)
 *
 * [Java와 비교]
 * - extends → : (콜론으로 상속)
 * - Optional<Post> → Post? (nullable)
 * - List, Page 반환 타입은 동일
 */
@Repository
interface PostRepository : JpaRepository<Post, Long>, PostRepositoryCustom {
    // QueryDSL을 사용한 복잡한 동적 쿼리 - PostRepositoryImpl에서 구현

    // 작성자로 게시글 조회
    // SELECT * FROM posts WHERE author = ?
    fun findByAuthor(author: String): List<Post>

    // 제목으로 검색 (페이징)
    // SELECT * FROM posts WHERE title LIKE %keyword%
    fun findByTitleContaining(keyword: String, pageable: Pageable): Page<Post>

    // 제목 또는 내용으로 검색 (JPQL 사용)
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    fun searchByKeyword(@Param("keyword") keyword: String, pageable: Pageable): Page<Post>

    // 댓글까지 함께 조회 (N+1 문제 해결)
    // Fetch Join으로 한 번에 로딩
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.comments WHERE p.id = :id")
    fun findByIdWithComments(@Param("id") id: Long): Post?

}