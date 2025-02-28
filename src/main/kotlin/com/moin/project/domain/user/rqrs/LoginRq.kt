package com.moin.project.domain.user.rqrs

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRq(

    @field:NotBlank
    @field:Email
    @JsonProperty("userId")
    @Schema(description = "유저 이메일 ID")
    private val _userId: String?,

    @field:NotBlank
    @Schema(description = "유저 PW")
    val password: String? = null
) {
    val userId: String
        get() = _userId!!
}
