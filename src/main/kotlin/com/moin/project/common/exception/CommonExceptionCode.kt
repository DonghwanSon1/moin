package com.moin.project.common.exception

import org.springframework.http.HttpStatus

enum class CommonExceptionCode(
        val status: HttpStatus,
        val message: String
) {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "입력값을 확인해주세요."),
    DUPLICATE_ID(HttpStatus.BAD_REQUEST, "중복된 아이디가 있습니다."),
    LOGIN_FAIL(HttpStatus.UNAUTHORIZED, "해당 유저가 존재하지 않거나, 아이디 또는 비밀번호가 틀렸습니다."),
    NOT_EXIST_USER_ID(HttpStatus.BAD_REQUEST, "해당 유저는 존재하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    DUPLICATE_DATA_ERROR(HttpStatus.CONFLICT, "중복 데이터 발생했습니다. 입력값을 확인 해주세요."),
    CONSTRAINTS_ERROR(HttpStatus.BAD_REQUEST, "데이터 처리 중 오류가 발생했습니다. 입력값을 확인한 후 다시 시도해주세요."),
    NEGATIVE_NUMBER(HttpStatus.BAD_REQUEST, "보낼/받을 값 중 마이너스가 발생했습니다. 다시 요청 부탁드립니다."),
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "환율 API 가 예상치 못한 오류가 발생했습니다."),
    NOT_EXIST_QUOTE(HttpStatus.BAD_REQUEST, "해당 견적서가 존재하지 않습니다."),
    QUOTE_EXPIRED(HttpStatus.BAD_REQUEST, "견적서의 유효시간이 만료되었습니다."),
    LIMIT_EXCESS(HttpStatus.BAD_REQUEST, "하루의 송금할 수 있는 금액이 초과되었습니다."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 오류가 발생했습니다."),

}