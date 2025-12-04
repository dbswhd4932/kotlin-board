package com.example.board.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 댓글 Entity (Kotlin 버전)
 *
 * [Java와 비교]
 * - open class 사용: JPA는 프록시 생성을 위해 상속 가능한 클래스 필요
 * - 생성자에서 바로 프로퍼티 선언 (Java의 120줄 → Kotlin 40줄)
 * - var post: Post?: 연관관계 설정을 위해 var 사용
 * - QueryDSL Q 클래스 생성을 위해 kapt 사용
 */
@Entity
@Table(name = "comments")
class Comment(

    // ID (DB가 자동 생성하므로 nullable)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // 댓글 내용 (변경 가능 - var)
    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    // 작성자 (변경 가능 - var)
    @Column(nullable = false, length = 50)
    var author: String,

    // 생성일시 (불변 - val)
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    // 수정일시 (변경 가능 - var)
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    // 게시글 (다대일 관계, 지연 로딩)
    // var: 연관관계 설정을 위해 변경 가능해야 함
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    var post: Post? = null

) {
    // 댓글 수정
    fun update(content: String) {
        this.content = content
        this.updatedAt = LocalDateTime.now()
    }

    // JPA Entity는 id 기반 equals/hashCode 필요
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Comment
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
