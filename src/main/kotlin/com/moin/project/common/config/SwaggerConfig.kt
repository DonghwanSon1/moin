package com.moin.project.common.config

import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.springdoc.core.customizers.OperationCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@SecurityScheme(
    name = "bearerAuth", // 인증 이름
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",  // 인증 방식
    bearerFormat = "JWT",  // 토큰 형식
    `in` = SecuritySchemeIn.HEADER,  // 헤더에서 인증 정보 받기
    paramName = "Authorization"  // Authorization 헤더에 토큰을 넣음
)
class SwaggerConfig {
    @Bean
    fun swaggerUiConfig(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("v1")
            .pathsToMatch("/**")
            .addOperationCustomizer(globalHeader())
            .build()
    }

    // API 요청에 SecurityRequirement 추가 (모든 요청에 대해 JWT 인증 요구)
    @Bean
    fun globalHeader(): OperationCustomizer {
        return OperationCustomizer { operation: Operation, _ ->
            operation.addSecurityItem(SecurityRequirement().addList("bearerAuth"))
            operation
        }
    }

}