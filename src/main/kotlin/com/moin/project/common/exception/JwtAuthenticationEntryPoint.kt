package com.moin.project.common.exception

import com.fasterxml.jackson.databind.ObjectMapper
import com.moin.project.common.response.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {
  private val objectMapper = ObjectMapper()

  override fun commence(
    request: HttpServletRequest,
    response: HttpServletResponse,
    authException: AuthenticationException
  ) {
    val invalidTokenException = CommonExceptionCode.INVALID_TOKEN

    val errorResponse = ErrorResponse(
      status = invalidTokenException.status.value(),
      message = invalidTokenException.message
    )

    response.status = HttpServletResponse.SC_UNAUTHORIZED
    response.contentType = "application/json;charset=UTF-8"
    response.writer.write(objectMapper.writeValueAsString(errorResponse))
    response.flushBuffer()
  }
}