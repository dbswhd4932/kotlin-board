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

    @Operation(
        summary = "배포 정보 확인",
        description = "현재 배포된 버전과 배포 시간을 확인합니다"
    )
    @GetMapping("/deploy-info")
    fun getDeployInfo(): ResponseEntity<Map<String, Any>> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        return ResponseEntity.ok(
            mapOf(
                "version" to "v1.0.0",
                "environment" to "production",
                "deployedAt" to deployTime.format(formatter),
                "uptime" to "${java.time.Duration.between(deployTime, LocalDateTime.now()).toMinutes()}분",
                "requestCount" to requestCount,
                "status" to "✅ CI/CD 자동 배포 성공!",
                "message" to "GitHub Actions를 통해 자동 배포되었습니다."
            )
        )
    }

    @Operation(
        summary = "시스템 정보",
        description = "서버의 시스템 정보를 확인합니다"
    )
    @GetMapping("/system")
    fun getSystemInfo(): ResponseEntity<Map<String, String>> {
        val runtime = Runtime.getRuntime()
        val mb = 1024 * 1024

        return ResponseEntity.ok(
            mapOf(
                "javaVersion" to System.getProperty("java.version"),
                "osName" to System.getProperty("os.name"),
                "osArch" to System.getProperty("os.arch"),
                "totalMemoryMB" to "${runtime.totalMemory() / mb}",
                "freeMemoryMB" to "${runtime.freeMemory() / mb}",
                "maxMemoryMB" to "${runtime.maxMemory() / mb}",
                "processors" to "${runtime.availableProcessors()}"
            )
        )
    }

    @Operation(
        summary = "CI/CD 테스트",
        description = "코드 수정 후 자동 배포가 잘 되는지 테스트합니다. 이 메시지가 바뀌면 배포 성공!"
    )
    @GetMapping("/cicd-test")
    fun cicdTest(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "testNumber" to 1,
                "message" to "🚀 첫 번째 자동 배포 테스트",
                "instruction" to "이 메시지를 수정하고 git push하면 자동으로 배포됩니다!",
                "deployedAt" to LocalDateTime.now().toString(),
                "success" to true
            )
        )
    }
}
