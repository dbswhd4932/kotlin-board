package com.example.board.repository

import com.example.board.entity.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

/**
 * Java의 PostRepositoryCustom 인터페이스를 Kotlin으로 변환
 *
 * 학습 목표:
 * 1. Kotlin에서 인터페이스 정의하기
 * 2. nullable 파라미터 사용 (Java의 null 허용 파라미터를 Kotlin의 ? 타입으로)
 * 3. 기본값이 있는 파라미터 활용 (선택적 파라미터)
 *
 * Kotlin 변환 포인트:
 * 1. nullable 파라미터
 *    Java: String titleContains (null 가능)
 *    Kotlin: titleContains: String? (nullable 타입)
 *
 * 2. List 타입
 *    Java: List<String> authorsIn
 *    Kotlin: authorsIn: List<String>?
 *
 * 3. 기본값 파라미터 (선택적)
 *    Kotlin에서는 기본값을 설정할 수 있어 메서드 오버로딩 불필요
 *    fun searchPosts(keyword: String? = null, pageable: Pageable)
 *
 * 4. 반환 타입
 *    Java: Map<String, Long>
 *    Kotlin: Map<String, Long> (동일)
 */
interface PostRepositoryCustom {

    /**
     * 복잡한 검색 조건을 사용한 게시글 검색
     *
     * 모든 조건이 optional이며 null이 아닌 조건만 적용됨
     *
     * @param titleContains 제목에 포함될 문자열 (nullable)
     * @param contentContains 내용에 포함될 문자열 (nullable)
     * @param authorEquals 작성자 정확히 일치 (nullable)
     * @param authorsIn 작성자 목록 중 하나 (nullable)
     * @param createdAfter 생성일 시작 범위 (nullable)
     * @param createdBefore 생성일 종료 범위 (nullable)
     * @param updatedAfter 수정일 시작 범위 (nullable)
     * @param updatedBefore 수정일 종료 범위 (nullable)
     * @param minCommentCount 최소 댓글 수 (nullable)
     * @param maxCommentCount 최대 댓글 수 (nullable)
     * @param keyword 키워드 (제목 OR 내용) (nullable)
     * @param pageable 페이징 정보
     * @return 검색된 게시글 Page
     */
    fun searchPosts(
        titleContains: String? = null,
        contentContains: String? = null,
        authorEquals: String? = null,
        authorsIn: List<String>? = null,
        createdAfter: LocalDateTime? = null,
        createdBefore: LocalDateTime? = null,
        updatedAfter: LocalDateTime? = null,
        updatedBefore: LocalDateTime? = null,
        minCommentCount: Int? = null,
        maxCommentCount: Int? = null,
        keyword: String? = null,
        pageable: Pageable
    ): Page<Post>

    /**
     * 제목, 내용, 작성자로 동적 검색
     */
    fun searchByTitleContentAuthor(
        title: String?,
        content: String?,
        author: String?,
        pageable: Pageable
    ): Page<Post>

    /**
     * 날짜 범위로 검색
     */
    fun searchByDateRange(
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        pageable: Pageable
    ): Page<Post>

    /**
     * 댓글이 있는 게시글만 조회
     */
    fun findPostsWithComments(pageable: Pageable): Page<Post>

    /**
     * 댓글이 없는 게시글만 조회
     */
    fun findPostsWithoutComments(pageable: Pageable): Page<Post>

    /**
     * 작성자별 게시글 수 통계
     */
    fun countPostsByAuthor(): Map<String, Long>

    /**
     * 최근 N일 이내 작성된 게시글 조회
     */
    fun findRecentPosts(days: Int, pageable: Pageable): Page<Post>

    /**
     * 인기 게시글 조회 (댓글 수 기준)
     */
    fun findPopularPosts(limit: Int): List<Post>

    /**
     * 제목 또는 내용에 키워드 포함
     */
    fun searchTitleOrContent(keyword: String, pageable: Pageable): Page<Post>

    /**
     * 특정 댓글 수 범위의 게시글 조회
     */
    fun findByCommentCountBetween(
        minCount: Int?,
        maxCount: Int?,
        pageable: Pageable
    ): Page<Post>

    /**
     * 여러 작성자의 게시글 조회
     */
    fun findByAuthors(authors: List<String>, pageable: Pageable): Page<Post>
}
