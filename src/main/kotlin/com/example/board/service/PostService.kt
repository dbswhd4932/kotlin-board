package com.example.board.service

import com.example.board.dto.PostDto
import com.example.board.repository.PostLikeRepository
import com.example.board.repository.PostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostService(
    private val postRepositoryKt: PostRepository,
    private val postLikeRepository: PostLikeRepository
) {

    /**
     * 동기 방식 - 순차 실행 (성능 비교용)
     */
    @Transactional(readOnly = true)
    fun getPostSync(id: Long): PostDto.PostDetailResponse {
        val startTime = System.currentTimeMillis()

        // 1. 게시글 + 댓글 조회 (순차)
        val post = postRepositoryKt.findByIdWithComments(id)
            ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다. id: $id")

        // 2. 좋아요 개수 조회 (순차)
        val likeCount = postLikeRepository.countByPostId(id)

        val duration = System.currentTimeMillis() - startTime
        println("[동기 방식] 실행 시간: ${duration}ms")

        return PostDto.PostDetailResponse.of(post, post.comments, likeCount)
    }

    /**
     * Coroutines 방식 - 병렬 실행 (성능 비교용)
     */
    @Transactional(readOnly = true)
    suspend fun getPostWithCoroutines(id: Long): PostDto.PostDetailResponse = coroutineScope {
        val startTime = System.currentTimeMillis()

        // 1. 게시글 + 댓글 조회 (병렬)
        // Dispatchers.IO 사용 (별도 스레드 풀에서 실행)
        val postDeferred = async(Dispatchers.IO) {
            postRepositoryKt.findByIdWithComments(id)
                ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다. id: $id")
        }

        // 2. 좋아요 개수 조회 (병렬)
        val likeCountDeferred = async(Dispatchers.IO) {
            postLikeRepository.countByPostId(id)
        }

        // await(): 비동기 작업 완료 대기
        val post = postDeferred.await()
        val likeCount = likeCountDeferred.await()

        val duration = System.currentTimeMillis() - startTime
        println("[Coroutines 방식] 실행 시간: ${duration}ms")

        PostDto.PostDetailResponse.of(post, post.comments, likeCount)
    }

    // 게시글 목록 조회 (페이징)
    @Transactional(readOnly = true)
    fun getPosts(pageable: Pageable): PostDto.PostListResponse {
        val page = postRepositoryKt.findAll(pageable)

        return PostDto.PostListResponse(
            posts = page.content.map {
                PostDto.PostResponse.from(it) // it -> 현재요소
            },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
            size = page.size
        )
    }

    // 게시글 상세 조회
    fun getPost(id: Long): PostDto.PostDetailResponse {
        val post = postRepositoryKt.findByIdWithComments(id)
            ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다. Id: $id")

        return PostDto.PostDetailResponse.from(post)
    }

    // 게시글 검색
    fun searchPost(keyword: String, pageable: Pageable): PostDto.PostListResponse {
        val page = postRepositoryKt.searchByKeyword(keyword, pageable)

        return PostDto.PostListResponse(
            posts = page.content.map {
                PostDto.PostResponse.from(it)
            },
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number,
            size = page.size
        )
    }

    // 게시글 생성
    @Transactional
    fun createPost(request: PostDto.CreatePostRequest): PostDto.PostResponse {
        val post = request.toEntity()
        val savedPost = postRepositoryKt.save(post)

        return PostDto.PostResponse.from(savedPost)
    }

    // 게시글 수정
    @Transactional
    fun updatePost(id: Long, request: PostDto.UpdatePostRequest): PostDto.PostResponse {
        val post = postRepositoryKt.findByIdOrNull(id)
            ?: throw IllegalArgumentException("게시글을 찾을 수 없습니다. id: $id")

        // 더티체킹 발생
        post.update(request.title, request.content)
        return PostDto.PostResponse.from(post)

    }

    // 게시글 삭제
    @Transactional
    fun deletePost(id: Long) {
        if (!postRepositoryKt.existsById(id)) {
            throw IllegalArgumentException("게시글을 찾을 수 없습니다. id: $id")
        }

        postRepositoryKt.deleteById(id)
    }
}