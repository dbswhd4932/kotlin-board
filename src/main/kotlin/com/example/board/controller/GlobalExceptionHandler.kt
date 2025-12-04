package com.example.board.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

/**
 * Java의 GlobalExceptionHandler를 Kotlin으로 변환
 *
 * 학습 목표:
 * 1. @RestControllerAdvice 어노테이션 사용법
 * 2. @ExceptionHandler를 통한 예외 처리
 * 3. Data Class를 활용한 에러 응답 DTO 생성
 * 4. Kotlin의 Collection API (map, forEach 등) 활용
 *
 * 구현해야 할 기능:
 * - MethodArgumentNotValidException 처리 (Validation 실패)
 * - IllegalArgumentException 처리 (잘못된 인자)
 * - Exception 처리 (기타 모든 예외)
 *
 * Kotlin 변환 포인트:
 * 1. ErrorResponse DTO를 data class로 변환
 *    Java: public static class ErrorResponse { ... } (getter/setter 필요)
 *    Kotlin: data class ErrorResponse(...) (getter/setter 자동 생성)
 *
 * 2. Map 생성 및 조작
 *    Java: Map<String, String> errors = new HashMap<>();
 *          errors.put(fieldName, errorMessage);
 *    Kotlin: val errors = mutableMapOf<String, String>()
 *            errors[fieldName] = errorMessage
 *    또는: val errors = ex.bindingResult.allErrors.associate { ... }
 *
 * 3. forEach 람다 표현식
 *    Java: ex.getBindingResult().getAllErrors().forEach(error -> { ... });
 *    Kotlin: ex.bindingResult.allErrors.forEach { error -> ... }
 *
 * 4. 타입 캐스팅
 *    Java: (FieldError) error
 *    Kotlin: error as FieldError
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * Validation 예외 처리
     *
     * 구현 힌트:
     * 1. ex.bindingResult.allErrors를 순회하며 에러 정보 수집
     * 2. FieldError로 캐스팅하여 field와 defaultMessage 추출
     * 3. ErrorResponse 객체 생성하여 반환
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        // Kotlin의 associate 함수를 사용하여 Map 생성 (더 간결한 방법)
        val errors = ex.bindingResult.fieldErrors.associate { fieldError ->
            fieldError.field to (fieldError.defaultMessage ?: "")
        }

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "입력값 검증에 실패했습니다",
            errors = errors,
            timestamp = LocalDateTime.now()
        )

        return ResponseEntity.badRequest().body(errorResponse)
    }

    /**
     * IllegalArgumentException 처리
     *
     * 구현 힌트:
     * 1. ex.message를 사용하여 에러 메시지 추출
     * 2. ErrorResponse 객체 생성 (errors는 빈 Map)
     * 3. HTTP 400 Bad Request로 반환
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "잘못된 요청입니다",
            errors = emptyMap(),
            timestamp = LocalDateTime.now()
        )

        return ResponseEntity.badRequest().body(errorResponse)
    }

    /**
     * 기타 모든 예외 처리
     *
     * 구현 힌트:
     * 1. 일반적인 서버 에러 메시지 반환
     * 2. HTTP 500 Internal Server Error로 반환
     */
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        ex.printStackTrace() // 실제 에러 출력

        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = ex.message ?: "서버 내부 오류가 발생했습니다",
            errors = emptyMap(),
            timestamp = LocalDateTime.now()
        )

        return ResponseEntity.internalServerError().body(errorResponse)
    }

    /**
     * 에러 응답 Data Class
     * Kotlin의 data class를 사용하면 getter/setter가 자동으로 생성됨
     */
    data class ErrorResponse(
        val status: Int,
        val error: String,
        val message: String,
        val errors: Map<String, String>,
        val timestamp: LocalDateTime
    )
}
