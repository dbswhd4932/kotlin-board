package com.example.board.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 게시글 Entity (Kotlin 버전)
 *
 * [Java와 비교]
 * - open class 사용: JPA는 프록시 생성을 위해 상속 가능한 클래스 필요
 * - val/var 구분: val은 불변(final), var는 가변
 * - 생성자 파라미터에서 바로 프로퍼티 선언
 * - getter/setter 자동 생성 (Java의 150줄 → Kotlin 50줄)
 * - QueryDSL Q 클래스 생성을 위해 kapt 사용
 */
@Entity
@Table(name = "posts")
class Post(

    // ID (DB가 자동 생성하므로 nullable)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // 제목 (변경 가능 - var)
    @Column(nullable = false, length = 200)
    var title: String,

    // 내용 (변경 가능 - var)
    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    // 작성자 (변경 가능 - var)
    @Column(nullable = false, length = 50)
    var author: String,

    // 생성일시 (불변 - val, 기본값 설정)
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    // 수정일시 (변경 가능 - var, 기본값 설정)
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    // 댓글 목록 (일대다 관계)
    // MutableList: 수정 가능한 리스트 (ArrayList 대신 인터페이스 사용)
    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    val comments: MutableList<Comment> = mutableListOf()

) {
    // 비즈니스 메서드

    // 댓글 추가
    fun addComment(comment: Comment) {
        comments.add(comment)
        comment.post = this
    }

    // 댓글 제거
    fun removeComment(comment: Comment) {
        comments.remove(comment)
        comment.post = null
    }

    // 게시글 수정
    fun update(title: String, content: String) {
        this.title = title
        this.content = content
        this.updatedAt = LocalDateTime.now()
    }

    // JPA Entity는 id 기반 equals/hashCode 필요
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Post
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
