package com.example.board.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.system.measureTimeMillis

/**
 * Coroutines 성능 비교 테스트
 *
 * [목적]
 * - 동기 방식 vs Coroutines 방식의 성능 차이 측정
 * - 병렬 실행의 효과 확인
 *
 * [테스트 방법]
 * 1. 테스트 데이터 준비 (게시글 + 댓글 + 좋아요)
 * 2. 동기 방식으로 10회 실행 → 평균 시간 측정
 * 3. Coroutines 방식으로 10회 실행 → 평균 시간 측정
 * 4. 결과 비교 출력
 *
 * 결과
 * 1. coroutines의 오버헤드
 * 2. 작업 시간이 너무 짧을 때 > 코루틴의 오버헤드(더 많은 객체 생성 등)로 더 느릴 수 있음.
 */
@SpringBootTest
class PostServicePerformanceTest @Autowired constructor(
    private val postService: PostService
) {

    private var testPostId: Long = 0

    @BeforeEach
    fun setup() {
        // data.sql에서 이미 ID 1번 게시글이 생성되어 있음
        // 댓글 10개, 좋아요 5개 포함
        testPostId = 1L
        println("테스트 데이터 사용: postId=$testPostId (data.sql에서 로드됨)")
    }

    @Test
    @DisplayName("동기 방식 vs Coroutines 성능 비교")
    fun `compare sync vs coroutines performance`() {
        val iterations = 500 // 반복 횟수

        println("\n" + "=".repeat(60))
        println("성능 비교 테스트 시작 (반복: ${iterations}회)")
        println("=".repeat(60))

        // ===== 동기 방식 테스트 =====
        val syncTimes = mutableListOf<Long>()
        println("\n[동기 방식 테스트]")

        repeat(iterations) { i ->
            val time = measureTimeMillis {
                postService.getPostSync(testPostId)
            }
            syncTimes.add(time)
            println("  ${i + 1}회: ${time}ms")
        }

        val syncAvg = syncTimes.average()
        println("평균: ${String.format("%.2f", syncAvg)}ms")

        // ===== Coroutines 방식 테스트 =====
        val asyncTimes = mutableListOf<Long>()
        println("\n[Coroutines 방식 테스트]")

        repeat(iterations) { i ->
            val time = measureTimeMillis {
                runBlocking {
                    postService.getPostWithCoroutines(testPostId)
                }
            }
            asyncTimes.add(time)
            println("  ${i + 1}회: ${time}ms")
        }

        val asyncAvg = asyncTimes.average()
        println("평균: ${String.format("%.2f", asyncAvg)}ms")

        // ===== 결과 비교 =====
        println("\n" + "=".repeat(60))
        println("결과 요약")
        println("=".repeat(60))
        println("동기 방식 평균:       ${String.format("%.2f", syncAvg)}ms")
        println("Coroutines 방식 평균: ${String.format("%.2f", asyncAvg)}ms")

        val improvement = ((syncAvg - asyncAvg) / syncAvg * 100)
        if (improvement > 0) {
            println("성능 향상:           ${String.format("%.2f", improvement)}%")
        } else {
            println("성능 차이:           ${String.format("%.2f", -improvement)}% (더 느림)")
        }
        println("=".repeat(60) + "\n")
    }

    @Test
    @DisplayName("단일 실행 비교 (로그 확인용)")
    fun `single execution comparison`() {
        println("\n" + "=".repeat(60))
        println("단일 실행 테스트 (로그 확인)")
        println("=".repeat(60))

        println("\n--- 동기 방식 실행 ---")
        postService.getPostSync(testPostId)

        println("\n--- Coroutines 방식 실행 ---")
        runBlocking {
            postService.getPostWithCoroutines(testPostId)
        }

        println("\n" + "=".repeat(60) + "\n")
    }
}
