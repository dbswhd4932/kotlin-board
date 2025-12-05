package com.example.board.controller

import com.example.board.dto.PostDto
import com.example.board.dto.toPageable
import com.example.board.service.PostService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/posts/kt")
@Tag(name = "게시글 API", description = "게시글 CRUD 및 검색 API")
class PostController(

    private val postServiceKt: PostService

) {
    @Operation(
        summary = "게시글 목록 조회",
        description = "페이징과 정렬이 적용된 게시글 목록을 조회합니다."
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = [Content(schema = Schema(implementation = PostDto.PostListResponse::class))]
        )
    )
    @GetMapping
    fun getPosts(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") size: Int,
        @Parameter(description = "정렬 기준 필드", example = "createdAt")
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @Parameter(description = "정렬 방향", example = "DESCENDING")
        @RequestParam(defaultValue = "DESCENDING") direction: String
    ): ResponseEntity<PostDto.PostListResponse> {

        val sortDirection = try {
            PostDto.SortDirection.valueOf(direction.uppercase())
        } catch (_: IllegalArgumentException) {
            PostDto.SortDirection.DESCENDING
        }

        val request = PostDto.PageRequest(page, size, sortBy, sortDirection)
        val pageable = request.toPageable()
        val response = postServiceKt.getPosts(pageable)

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "게시글 상세 조회", description = "ID로 게시글 상세 정보를 조회합니다 (댓글 포함)")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    )
    @GetMapping("/{id}")
    fun getPost(
        @Parameter(description = "게시글 ID", example = "1")
        @PathVariable id: Long
    ): ResponseEntity<PostDto.PostDetailResponse> {
        val response = postServiceKt.getPost(id)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "게시글 검색", description = "키워드로 게시글을 검색합니다 (제목 + 내용)")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "검색 성공")
    )
    @GetMapping("/search")
    fun searchPost(
        @Parameter(description = "검색 키워드", example = "Kotlin", required = true)
        @RequestParam keyword: String,
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") size: Int,
        @Parameter(description = "정렬 기준 필드", example = "createdAt")
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @Parameter(description = "정렬 방향", example = "DESCENDING")
        @RequestParam(defaultValue = "DESCENDING") direction: String
    ): ResponseEntity<PostDto.PostListResponse> {
        val sortDirection = try {
            PostDto.SortDirection.valueOf(direction.uppercase())
        } catch (_: IllegalArgumentException) {
            PostDto.SortDirection.DESCENDING
        }

        val request = PostDto.PageRequest(page, size, sortBy, sortDirection)
        val pageable = request.toPageable()
        val response = postServiceKt.searchPost(keyword, pageable)

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "게시글 생성", description = "새로운 게시글을 작성합니다")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "생성 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청 (validation 실패)")
    )
    @PostMapping
    fun createPost(
        @Parameter(description = "게시글 생성 요청", required = true)
        @Valid @RequestBody request: PostDto.CreatePostRequest
    ): ResponseEntity<PostDto.PostResponse> {
        val response = postServiceKt.createPost(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "수정 성공"),
        ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    )
    @PutMapping("/{id}")
    fun updatePost(
        @Parameter(description = "게시글 ID", example = "1")
        @PathVariable id: Long,
        @Parameter(description = "게시글 수정 요청", required = true)
        @Valid @RequestBody request: PostDto.UpdatePostRequest
    ): ResponseEntity<PostDto.PostResponse> {
        val response = postServiceKt.updatePost(id, request)
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "삭제 성공"),
        ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    )
    @DeleteMapping("/{id}")
    fun deletePost(
        @Parameter(description = "게시글 ID", example = "1")
        @PathVariable id: Long
    ): ResponseEntity<Unit> {
        postServiceKt.deletePost(id)
        return ResponseEntity.noContent().build()
    }

    // ==================== 성능 비교용 엔드포인트 ====================

    @Operation(
        summary = "[성능비교] 동기 방식 조회",
        description = "순차적으로 게시글과 댓글, 좋아요를 조회합니다 (성능 비교용)"
    )
    @GetMapping("/{id}/sync")
    fun getPostSync(
        @Parameter(description = "게시글 ID", example = "1")
        @PathVariable id: Long
    ): ResponseEntity<PostDto.PostDetailResponse> {
        val response = postServiceKt.getPostSync(id)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "[성능비교] Coroutines 비동기 조회",
        description = "병렬로 게시글과 댓글, 좋아요를 조회합니다 (성능 비교용)"
    )
    @GetMapping("/{id}/async")
    suspend fun getPostAsync(
        @Parameter(description = "게시글 ID", example = "1")
        @PathVariable id: Long
    ): ResponseEntity<PostDto.PostDetailResponse> {
        val response = postServiceKt.getPostWithCoroutines(id)
        return ResponseEntity.ok(response)
    }

    // ==================== CI/CD 배포 테스트용 ====================

    @Operation(
        summary = "[배포테스트] 버전 정보",
        description = "현재 배포된 애플리케이션의 버전과 배포 시간을 확인합니다"
    )
    @GetMapping("/version")
    fun getVersion(): ResponseEntity<Map<String, Any>> {
        val version = mapOf(
            "version" to "1.0.1",
            "deployedAt" to java.time.LocalDateTime.now().toString(),
            "status" to "CI/CD 자동 배포 성공!",
            "message" to "이 엔드포인트가 보이면 자동 배포가 완료된 것입니다!"
        )
        return ResponseEntity.ok(version)
    }

}