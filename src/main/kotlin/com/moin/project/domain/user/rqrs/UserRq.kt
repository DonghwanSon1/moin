package com.moin.project.domain.user.rqrs

import com.fasterxml.jackson.annotation.JsonProperty
import com.moin.project.domain.user.enums.Role
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UserRq(

    @field:NotBlank
    @field:Email
    @JsonProperty("userId")
    @Schema(description = "유저 이메일 ID")
    private val _userId: String?,

    @field:NotBlank
    @Schema(description = "유저 PW")
    val password: String,

    @field:NotBlank
    @Schema(description = "유저 이름")
    val name: String,

    @field:Valid
    @Schema(description = "유저 역할")
    val idType: Role,

    @field:NotBlank
    @Schema(description = "유저 주민/사업자 등록번호")
    val idValue: String
) {
    val userId: String
        get() = _userId!!
}
