package com.moin.project.common.exception

import com.moin.project.common.response.ErrorResponse
import org.hibernate.exception.ConstraintViolationException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.sql.SQLException

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(CommonException::class)
    fun handleCommonException(ex: CommonException): ResponseEntity<ErrorResponse> {
        val status = ex.exceptionCode.status
        val response = ErrorResponse(status.value(), ex.exceptionCode.message)

        return ResponseEntity(response, status)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleException(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val sqlException = ex.cause as? SQLException
        // 유니크 제약조건 위배가 아니면 나머지는 공통 ExceptionCode 로 응답한다.
        val exceptionCode = if (sqlException?.sqlState == "23505") {
            // 유니크 제약조건 위배 시
            CommonExceptionCode.DUPLICATE_DATA_ERROR
        } else {
            // 나머지 제약조건 위배 시
            CommonExceptionCode.CONSTRAINTS_ERROR
        }

        val status = exceptionCode.status
        val response = ErrorResponse(status.value(), exceptionCode.message)

        return ResponseEntity(response, status)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val exceptionCode = CommonExceptionCode.BAD_REQUEST

        val status = exceptionCode.status
        val response = ErrorResponse(
            status.value(),exceptionCode.message)

        return ResponseEntity(response, status)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val exceptionCode = CommonExceptionCode.BAD_REQUEST
        val status = exceptionCode.status
        val response = ErrorResponse(
            status.value(),exceptionCode.message)

        return ResponseEntity(response, status)
    }
}