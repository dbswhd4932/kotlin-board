package com.example.board

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Spring Boot 애플리케이션 메인 클래스 (Kotlin 버전)
 *
 * [Java와 비교]
 * - class 대신 파일 레벨 함수 사용 가능
 * - SpringApplication.run() 대신 runApplication<T>() 확장 함수 사용
 * - main 함수를 최상위 레벨에 선언 (클래스 밖)
 */
@SpringBootApplication
class BoardApplication

/**
 * 애플리케이션 진입점
 *
 * Kotlin 스타일:
 * - main 함수를 최상위 레벨에 선언 (Java의 static과 유사)
 * - runApplication<BoardApplication>(*args): 타입 파라미터로 클래스 지정
 * - *args: spread operator로 vararg 전달
 */
fun main(args: Array<String>) {
    runApplication<BoardApplication>(*args)
}
