package com.example.board.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 게시글 좋아요 Entity (Kotlin 학습용)
 *
 * [학습 포인트]
 * 1. Data class vs Entity class 차이
 *    - Entity는 data class 사용 X (JPA 프록시 때문)
 *    - open class 사용 (상속 가능해야 함)
 *
 * 2. Primary key 복합키 vs 단순키
 *    - 여기서는 단순키(id) 사용
 *    - post + userId 조합은 unique 제약으로 처리
 *
 * 3. ManyToOne 관계
 *    - PostLike(N) : Post(1) - 여러 좋아요가 하나의 게시글에 속함
 *    - @ManyToOne: 다대일 관계 매핑
 *    - @JoinColumn: 외래키 컬럼 지정
 *
 * 4. Nullable 처리
 *    - id: Long? = null (DB가 생성하므로 null 허용)
 *    - post: Post (필수값, nullable 아님)
 *    - userId: Long (필수값, nullable 아님)
 */
@Entity
@Table(
    name = "post_likes",
    // TODO: 학습 포인트 - uniqueConstraints
    // 한 사용자가 같은 게시글에 중복 좋아요 방지
    // Java: @Table(uniqueConstraints = {@UniqueConstraint(...)})
    // Kotlin: uniqueConstraints = [UniqueConstraint(...)]
    uniqueConstraints = [
        // TODO: 학습 포인트 - UniqueConstraint
        // 한 사용자가 같은 게시글에 중복 좋아요 방지
        // UniqueConstraint(columnNames = ["post_id", "user_id"])
    ]
)
class PostLike(

    // TODO: 학습 포인트 - Primary Key
    // @Id: JPA 기본키 지정
    // @GeneratedValue: 자동 생성 전략
    // - IDENTITY: DB의 auto_increment 사용
    // - SEQUENCE: DB 시퀀스 사용
    // - AUTO: JPA가 자동 선택
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // TODO: 학습 포인트 - @ManyToOne (다대일 관계)
    // [기존 방식] postId: Long - 단순 ID만 저장
    // [개선 방식] post: Post - 실제 Post 엔티티와 연결
    //
    // @ManyToOne: 다대일 관계 선언
    // - fetch = FetchType.LAZY: 지연 로딩 (성능 최적화)
    // - fetch = FetchType.EAGER: 즉시 로딩 (PostLike 조회 시 Post도 함께 조회)
    //
    // @JoinColumn: 외래키 컬럼 지정
    // - name = "post_id": DB 테이블의 실제 컬럼명
    // - nullable = false: NOT NULL 제약 (게시글 필수)
    //
    // Java 예시:
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "post_id", nullable = false)
    // private Post post;
    //
    // TODO: 아래 주석을 해제하고 구현하세요
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "post_id", nullable = false)
    // val post: Post,

    // TODO: 위의 ManyToOne 관계를 구현하면 아래 postId는 삭제해야 합니다
    @Column(name = "post_id", nullable = false)
    val postId: Long, // 임시: ManyToOne 구현 후 삭제 예정

    // TODO: 학습 포인트 - 사용자 ID
    // 실제로는 User Entity와 @ManyToOne으로 연결해야 하지만
    // 학습을 위해 단순하게 Long으로 처리
    @Column(name = "user_id", nullable = false)
    val userId: Long, // TODO: 필수값

    // TODO: 학습 포인트 - Default Value
    // Kotlin에서는 기본값 설정 가능
    // Java: this.createdAt = LocalDateTime.now() (생성자에서)
    // Kotlin: = LocalDateTime.now() (선언과 동시에)
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

) {
    // TODO: 학습 포인트 - equals & hashCode
    // JPA Entity는 id 기반으로 동등성 비교 필요
    // - equals: 객체가 같은지 비교 (id로 비교)
    // - hashCode: HashMap, HashSet 등에서 사용
    //
    // [왜 필요한가?]
    // - Set에 PostLike를 담을 때 중복 체크
    // - Entity 비교 시 올바른 동작 보장
    //
    // [구현 패턴]
    // 1. this === other (동일 참조 체크)
    // 2. javaClass != other?.javaClass (타입 체크)
    // 3. id != null && id == other.id (id 비교)
    //
    // TODO: 아래 주석을 해제하고 구현하세요
    /*
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PostLike
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
    */

    // TODO: 학습 포인트 - toString (디버깅용)
    // data class가 아니므로 직접 구현
    // "PostLike(id=$id, postId=$postId, userId=$userId)" 형식
    //
    // ManyToOne 구현 후에는:
    // "PostLike(id=$id, postId=${post.id}, userId=$userId)"
    //
    // TODO: 아래 주석을 해제하고 구현하세요
    /*
    override fun toString(): String {
        return "PostLike(id=$id, postId=$postId, userId=$userId, createdAt=$createdAt)"
    }
    */
}
