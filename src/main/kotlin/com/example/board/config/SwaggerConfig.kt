package com.example.board.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Swagger(OpenAPI) 설정
 *
 * [학습 포인트]
 * 1. @Configuration: Spring Bean 설정 클래스
 * 2. @Bean: 메서드가 반환하는 객체를 Spring Bean으로 등록
 * 3. OpenAPI: Swagger/OpenAPI 3.0 스펙을 따르는 API 문서 설정
 *
 * [접속 URL]
 * - Swagger UI: http://localhost:8080/swagger-ui/index.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 *
 * [Kotlin 포인트]
 * - 메서드 체이닝을 활용한 fluent API
 * - listOf()로 간결하게 리스트 생성
 */
@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(apiInfo())
            .servers(serverList())
    }

    /**
     * API 기본 정보
     */
    private fun apiInfo(): Info {
        return Info()
            .title("Kotlin Board API")
            .description(
                """
                Kotlin 학습용 게시판 REST API 문서입니다.

                ### 주요 기능
                - 게시글 CRUD (페이징, 정렬, 검색)
                - 댓글 CRUD
                - 좋아요 기능
                - Coroutines 성능 비교

                ### 기술 스택
                - Kotlin 2.0
                - Spring Boot 3.2
                - JPA + QueryDSL
                - H2 Database
                """.trimIndent()
            )
            .version("v1.0.0")
            .contact(
                Contact()
                    .name("Board API Support")
            )
            .license(
                License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")
            )
    }

    /**
     * 서버 목록 설정
     */
    private fun serverList(): List<Server> {
        return listOf(
            Server()
                .url("http://localhost:8080")
                .description("로컬 개발 환경"),
            Server()
                .url("https://api.example.com")
                .description("운영 환경 (배포 후)")
        )
    }
}
