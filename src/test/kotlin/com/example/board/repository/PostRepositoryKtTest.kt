package com.example.board.repository

import com.example.board.entity.Comment
import com.example.board.entity.Post
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest

/**
 * PostRepository 테스트
 *
 * [@DataJpaTest]
 * - JPA 관련 컴포넌트만 로드 (경량 테스트)
 * - 각 테스트마다 트랜잭션 롤백 (테스트 격리)
 * - 내장 H2 DB 자동 설정
 *
 * [Kotlin 테스트 포인트]
 * - assertThat().isEqualTo() - AssertJ 사용
 * - lateinit var - 테스트 전에 초기화
 * - DisplayName - 한글로 테스트 설명
 */
@DataJpaTest
@DisplayName("PostRepository 테스트")
class PostRepositoryTest {

    @Autowired
    private lateinit var postRepositoryKt: PostRepository

    private lateinit var savedPost1: Post
    private lateinit var savedPost2: Post

    @BeforeEach
    fun setUp() {
        // Given: 테스트 데이터 준비
        val post1 = Post(
            title = "첫 번째 게시글",
            content = "첫 번째 내용",
            author = "홍길동"
        )

        val post2 = Post(
            title = "두 번째 게시글",
            content = "두 번째 내용",
            author = "김철수"
        )

        savedPost1 = postRepositoryKt.save(post1)
        savedPost2 = postRepositoryKt.save(post2)
    }

    @Test
    @DisplayName("게시글을 저장할 수 있다")
    fun savePost() {
        // Given
        val post = Post(
            title = "테스트 게시글",
            content = "테스트 내용",
            author = "테스터"
        )

        // When
        val savedPost = postRepositoryKt.save(post)

        // Then
        assertThat(savedPost.id).isNotNull()
        assertThat(savedPost.title).isEqualTo("테스트 게시글")
        assertThat(savedPost.content).isEqualTo("테스트 내용")
        assertThat(savedPost.author).isEqualTo("테스터")
    }

    @Test
    @DisplayName("ID로 게시글을 조회할 수 있다")
    fun findById() {
        // When
        val foundPost = postRepositoryKt.findById(savedPost1.id!!)

        // Then
        assertThat(foundPost).isPresent
        assertThat(foundPost.get().title).isEqualTo("첫 번째 게시글")
    }

    @Test
    @DisplayName("작성자로 게시글을 조회할 수 있다")
    fun findByAuthor() {
        // When
        val posts = postRepositoryKt.findByAuthor("홍길동")

        // Then
        assertThat(posts).hasSize(1)
        assertThat(posts[0].title).isEqualTo("첫 번째 게시글")
    }

    @Test
    @DisplayName("제목으로 게시글을 검색할 수 있다")
    fun findByTitleContaining() {
        // Given
        val pageable = PageRequest.of(0, 10)

        // When
        val page = postRepositoryKt.findByTitleContaining("첫 번째", pageable)

        // Then
        assertThat(page.content).hasSize(1)
        assertThat(page.content[0].title).contains("첫 번째")
    }

    @Test
    @DisplayName("키워드로 게시글을 검색할 수 있다 (제목 또는 내용)")
    fun searchByKeyword() {
        // Given
        val pageable = PageRequest.of(0, 10)

        // When: 제목에 있는 키워드 검색
        val pageByTitle = postRepositoryKt.searchByKeyword("첫 번째", pageable)

        // When: 내용에 있는 키워드 검색
        val pageByContent = postRepositoryKt.searchByKeyword("두 번째 내용", pageable)

        // Then
        assertThat(pageByTitle.content).hasSize(1)
        assertThat(pageByContent.content).hasSize(1)
    }

    @Test
    @DisplayName("게시글과 댓글을 함께 조회할 수 있다 (Fetch Join)")
    fun findByIdWithComments() {
        // Given: 댓글이 있는 게시글 생성
        val post = Post(
            title = "댓글 테스트",
            content = "내용",
            author = "작성자"
        )
        val savedPost = postRepositoryKt.save(post)

        val comment1 = Comment(
            content = "댓글1",
            author = "댓글작성자1"
        )
        val comment2 = Comment(
            content = "댓글2",
            author = "댓글작성자2"
        )

        savedPost.addComment(comment1)
        savedPost.addComment(comment2)
        postRepositoryKt.save(savedPost)

        // When: Fetch Join으로 조회
        val foundPost = postRepositoryKt.findByIdWithComments(savedPost.id!!)

        // Then
        assertThat(foundPost).isNotNull
        assertThat(foundPost!!.comments).hasSize(2)
        assertThat(foundPost.comments[0].content).isEqualTo("댓글1")
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 null을 반환한다")
    fun findByIdWithComments_NotFound() {
        // When
        val foundPost = postRepositoryKt.findByIdWithComments(999L)

        // Then
        assertThat(foundPost).isNull()
    }

    @Test
    @DisplayName("게시글을 삭제할 수 있다")
    fun deletePost() {
        // When
        postRepositoryKt.deleteById(savedPost1.id!!)

        // Then
        val foundPost = postRepositoryKt.findById(savedPost1.id!!)
        assertThat(foundPost).isEmpty
    }

    @Test
    @DisplayName("전체 게시글 수를 조회할 수 있다")
    fun count() {
        // When
        val count = postRepositoryKt.count()

        // Then
        assertThat(count).isEqualTo(2)
    }

    @Test
    @DisplayName("게시글 존재 여부를 확인할 수 있다")
    fun existsById() {
        // When
        val exists = postRepositoryKt.existsById(savedPost1.id!!)
        val notExists = postRepositoryKt.existsById(999L)

        // Then
        assertThat(exists).isTrue()
        assertThat(notExists).isFalse()
    }
}
