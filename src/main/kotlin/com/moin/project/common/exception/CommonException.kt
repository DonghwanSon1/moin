package com.moin.project.common.exception


class CommonException(val exceptionCode: CommonExceptionCode) : RuntimeException(exceptionCode.message) {

}