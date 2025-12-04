package com.example.board.repository

import com.example.board.entity.Post
import com.example.board.entity.QPost
import com.example.board.entity.QComment
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Java의 PostRepositoryImpl을 Kotlin으로 변환
 *
 * 학습 목표:
 * 1. QueryDSL을 Kotlin에서 사용하기
 * 2. BooleanBuilder를 활용한 동적 쿼리 작성
 * 3. let, apply, run 등 스코프 함수 활용
 * 4. Elvis 연산자를 활용한 null 처리
 * 5. Kotlin의 when 표현식 활용
 *
 * 구현해야 할 기능:
 * - PostRepositoryCustom 인터페이스의 모든 메서드 구현
 * - QueryDSL을 활용한 복잡한 동적 쿼리 작성
 *
 * Kotlin 변환 포인트:
 * 1. Q 클래스 초기화
 *    Java: private final QPost post = QPost.post;
 *    Kotlin: private val post = QPost.post
 *
 * 2. null 체크 및 조건 추가
 *    Java: if (titleContains != null && !titleContains.trim().isEmpty()) {
 *              builder.and(post.title.containsIgnoreCase(titleContains));
 *          }
 *    Kotlin: titleContains?.takeIf { it.isNotBlank() }?.let {
 *              builder.and(post.title.containsIgnoreCase(it))
 *            }
 *    또는: if (!titleContains.isNullOrBlank()) {
 *            builder.and(post.title.containsIgnoreCase(titleContains))
 *          }
 *
 * 3. Collection의 isEmpty 체크
 *    Java: if (authorsIn != null && !authorsIn.isEmpty())
 *    Kotlin: if (!authorsIn.isNullOrEmpty())
 *
 * 4. groupBy를 활용한 통계
 *    Java: posts.stream()
 *          .collect(Collectors.groupingBy(Post::getAuthor, Collectors.counting()))
 *    Kotlin: posts.groupBy { it.author }
 *                 .mapValues { it.value.size.toLong() }
 *
 * 5. Elvis 연산자로 기본값 제공
 *    Java: total != null ? total : 0L
 *    Kotlin: total ?: 0L
 */
/**
 * QueryDSL을 사용한 복잡한 동적 쿼리 구현
 * Kotlin Entity에서 kapt로 생성된 Q 클래스(QPost, QComment) 활용
 */
@Repository
class PostRepositoryImpl(
    entityManager: EntityManager
) : PostRepositoryCustom {

    private val queryFactory = JPAQueryFactory(entityManager)
    private val post = QPost.post
    private val comment = QComment.comment

    override fun searchPosts(
        titleContains: String?,
        contentContains: String?,
        authorEquals: String?,
        authorsIn: List<String>?,
        createdAfter: LocalDateTime?,
        createdBefore: LocalDateTime?,
        updatedAfter: LocalDateTime?,
        updatedBefore: LocalDateTime?,
        minCommentCount: Int?,
        maxCommentCount: Int?,
        keyword: String?,
        pageable: Pageable
    ): Page<Post> {
        val builder = BooleanBuilder()

        // 1. 제목 검색
        if (!titleContains.isNullOrBlank()) {
            builder.and(post.title.containsIgnoreCase(titleContains))
        }

        // 2. 내용 검색
        if (!contentContains.isNullOrBlank()) {
            builder.and(post.content.containsIgnoreCase(contentContains))
        }

        // 3. 작성자 정확히 일치
        if (!authorEquals.isNullOrBlank()) {
            builder.and(post.author.eq(authorEquals))
        }

        // 4. 작성자 목록 중 하나
        if (!authorsIn.isNullOrEmpty()) {
            builder.and(post.author.`in`(authorsIn))
        }

        // 5. 생성일 범위
        createdAfter?.let { builder.and(post.createdAt.goe(it)) }
        createdBefore?.let { builder.and(post.createdAt.loe(it)) }

        // 6. 수정일 범위
        updatedAfter?.let { builder.and(post.updatedAt.goe(it)) }
        updatedBefore?.let { builder.and(post.updatedAt.loe(it)) }

        // 7. 키워드 검색 (제목 OR 내용)
        if (!keyword.isNullOrBlank()) {
            builder.and(
                post.title.containsIgnoreCase(keyword)
                    .or(post.content.containsIgnoreCase(keyword))
            )
        }

        // 8. 댓글 수 범위 (LEFT JOIN + GROUP BY + HAVING으로 최적화)
        val hasCommentCountCondition = minCommentCount != null || maxCommentCount != null

        // 쿼리 실행
        val content = if (hasCommentCountCondition) {
            // 댓글 수 조건이 있을 때: LEFT JOIN + GROUP BY + HAVING 사용
            var query = queryFactory
                .selectFrom(post)
                .leftJoin(post.comments, comment)
                .where(builder)
                .groupBy(post.id)

            minCommentCount?.let { query = query.having(comment.count().goe(it.toLong())) }
            maxCommentCount?.let { query = query.having(comment.count().loe(it.toLong())) }

            query
                .orderBy(post.createdAt.desc())
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
        } else {
            // 댓글 수 조건이 없을 때: 단순 조회
            queryFactory
                .selectFrom(post)
                .where(builder)
                .orderBy(post.createdAt.desc())
                .offset(pageable.offset)
                .limit(pageable.pageSize.toLong())
                .fetch()
        }

        // 전체 개수 조회
        val total = if (hasCommentCountCondition) {
            // 댓글 수 조건이 있을 때: GROUP BY로 조회 후 개수 세기
            var countQuery = queryFactory
                .select(post.id)
                .from(post)
                .leftJoin(post.comments, comment)
                .where(builder)
                .groupBy(post.id)

            minCommentCount?.let { countQuery = countQuery.having(comment.count().goe(it.toLong())) }
            maxCommentCount?.let { countQuery = countQuery.having(comment.count().loe(it.toLong())) }

            countQuery.fetch().size.toLong()
        } else {
            // 댓글 수 조건이 없을 때: COUNT 쿼리
            queryFactory
                .select(post.count())
                .from(post)
                .where(builder)
                .fetchOne() ?: 0L
        }

        return PageImpl(content, pageable, total)
    }

    override fun searchByTitleContentAuthor(
        title: String?,
        content: String?,
        author: String?,
        pageable: Pageable
    ): Page<Post> {
        val builder = BooleanBuilder()

        if (!title.isNullOrBlank()) {
            builder.and(titleContains(title))
        }

        if (!content.isNullOrBlank()) {
            builder.and(contentContains(content))
        }

        if (!author.isNullOrBlank()) {
            builder.and(authorEquals(author))
        }

        val posts = queryFactory
            .selectFrom(post)
            .where(builder)
            .orderBy(post.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val total = queryFactory
            .select(post.count())
            .from(post)
            .where(builder)
            .fetchOne() ?: 0L

        return PageImpl(posts, pageable, total)
    }

    override fun searchByDateRange(
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        pageable: Pageable
    ): Page<Post> {
        val builder = BooleanBuilder()

        startDate?.let { builder.and(post.createdAt.goe(it)) }
        endDate?.let { builder.and(post.createdAt.loe(it)) }

        val posts = queryFactory
            .selectFrom(post)
            .where(builder)
            .orderBy(post.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val total = queryFactory
            .select(post.count())
            .from(post)
            .where(builder)
            .fetchOne() ?: 0L

        return PageImpl(posts, pageable, total)
    }

    override fun findPostsWithComments(pageable: Pageable): Page<Post> {
        // INNER JOIN으로 최적화 (댓글이 있는 게시글만 조회)
        val posts = queryFactory
            .selectFrom(post)
            .innerJoin(post.comments, comment)
            .distinct()  // 중복 제거 (게시글이 여러 댓글로 인해 중복될 수 있음)
            .orderBy(post.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val total = queryFactory
            .select(post.countDistinct())  // DISTINCT COUNT
            .from(post)
            .innerJoin(post.comments, comment)
            .fetchOne() ?: 0L

        return PageImpl(posts, pageable, total)
    }

    override fun findPostsWithoutComments(pageable: Pageable): Page<Post> {
        // LEFT JOIN + IS NULL로 최적화 (댓글이 없는 게시글 조회)
        val posts = queryFactory
            .selectFrom(post)
            .leftJoin(post.comments, comment)
            .where(comment.id.isNull())  // 댓글이 없는 경우
            .orderBy(post.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val total = queryFactory
            .select(post.count())
            .from(post)
            .leftJoin(post.comments, comment)
            .where(comment.id.isNull())
            .fetchOne() ?: 0L

        return PageImpl(posts, pageable, total)
    }

    override fun countPostsByAuthor(): Map<String, Long> {
        val posts = queryFactory
            .selectFrom(post)
            .fetch()

        return posts.groupBy { it.author }
            .mapValues { entry -> entry.value.size.toLong() }
    }

    override fun findRecentPosts(
        days: Int,
        pageable: Pageable
    ): Page<Post> {
        val startDate = LocalDateTime.now().minusDays(days.toLong())

        val posts = queryFactory
            .selectFrom(post)
            .where(post.createdAt.goe(startDate))
            .orderBy(post.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val total = queryFactory
            .select(post.count())
            .from(post)
            .where(post.createdAt.goe(startDate))
            .fetchOne() ?: 0L

        return PageImpl(posts, pageable, total)
    }

    override fun findPopularPosts(limit: Int): List<Post> {
        return queryFactory
            .selectFrom(post)
            .leftJoin(post.comments, comment)
            .groupBy(post.id)
            .orderBy(comment.count().desc(), post.createdAt.desc())
            .limit(limit.toLong())
            .fetch()
    }

    override fun searchTitleOrContent(
        keyword: String,
        pageable: Pageable
    ): Page<Post> {
        val posts = queryFactory
            .selectFrom(post)
            .where(
                post.title.containsIgnoreCase(keyword)
                    .or(post.content.containsIgnoreCase(keyword))
            )
            .orderBy(post.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val total = queryFactory
            .select(post.count())
            .from(post)
            .where(
                post.title.containsIgnoreCase(keyword)
                    .or(post.content.containsIgnoreCase(keyword))
            )
            .fetchOne() ?: 0L

        return PageImpl(posts, pageable, total)
    }

    override fun findByCommentCountBetween(
        minCount: Int?,
        maxCount: Int?,
        pageable: Pageable
    ): Page<Post> {
        var query = queryFactory
            .selectFrom(post)
            .leftJoin(post.comments, comment)
            .groupBy(post.id)

        minCount?.let { query = query.having(comment.count().goe(it.toLong())) }
        maxCount?.let { query = query.having(comment.count().loe(it.toLong())) }

        val posts = query
            .orderBy(post.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        var countQuery = queryFactory
            .select(post.count())
            .from(post)
            .leftJoin(post.comments, comment)
            .groupBy(post.id)

        minCount?.let { countQuery = countQuery.having(comment.count().goe(it.toLong())) }
        maxCount?.let { countQuery = countQuery.having(comment.count().loe(it.toLong())) }

        val total = countQuery.fetch().size.toLong()

        return PageImpl(posts, pageable, total)
    }

    override fun findByAuthors(
        authors: List<String>,
        pageable: Pageable
    ): Page<Post> {
        if (authors.isEmpty()) {
            return PageImpl(emptyList(), pageable, 0L)
        }

        val posts = queryFactory
            .selectFrom(post)
            .where(post.author.`in`(authors))
            .orderBy(post.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val total = queryFactory
            .select(post.count())
            .from(post)
            .where(post.author.`in`(authors))
            .fetchOne() ?: 0L

        return PageImpl(posts, pageable, total)
    }

    // ========== Private Helper Methods ==========

    private fun titleContains(title: String?): BooleanExpression? {
        return title?.let { post.title.containsIgnoreCase(it) }
    }

    private fun contentContains(content: String?): BooleanExpression? {
        return content?.let { post.content.containsIgnoreCase(it) }
    }

    private fun authorEquals(author: String?): BooleanExpression? {
        return author?.let { post.author.eq(it) }
    }
}
