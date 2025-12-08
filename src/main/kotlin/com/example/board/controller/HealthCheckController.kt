package com.example.board.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/api/health")
@Tag(name = "헬스체크 API", description = "CI/CD 배포 및 서버 상태 확인용 API")
class HealthCheckController {

    private val deployTime = LocalDateTime.now()
    private var requestCount = 0

    @Operation(
        summary = "서버 상태 확인",
        description = "서버가 정상 실행 중인지 확인합니다"
    )
    @GetMapping("/ping")
    fun ping(): ResponseEntity<Map<String, String>> {
        requestCount++
        return ResponseEntity.ok(
            mapOf(
                "status" to "OK",
                "message" to "pong! 서버가 정상 작동 중입니다.",
                "timestamp" to LocalDateTime.now().toString()
            )
        )
    }
}
