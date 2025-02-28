package com.moin.project.common.response

import org.springframework.http.HttpStatus

data class BaseResponse<T>(
    val status: Int = HttpStatus.OK.value(),
    val data: T? = null,
    val message: String? = "SUCCESS"
)
