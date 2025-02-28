package com.moin.project.domain.externalApi

import com.moin.project.common.exception.CommonException
import com.moin.project.common.exception.CommonExceptionCode
import com.moin.project.domain.externalApi.dto.ExternalApiDto
import com.moin.project.domain.transfer.enums.CurrencyInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class ExternalApiService(
  private val restTemplate: RestTemplate,

  @Value("\${exchangeRateUrl}")
  private val exchangeRateUrl: String
) {

  fun exchangeRateApi(param: String): List<ExternalApiDto> {
    val usdParam: String = CurrencyInfo.USD.externalApiParam
    val url = UriComponentsBuilder.fromHttpUrl(exchangeRateUrl)
      .queryParam("codes", "$usdParam,$param")
      .build(true)
      .toUri()

    val headers = HttpHeaders().apply {
      set("Accept", "application/json")
    }

    return try {
      restTemplate.exchange(
        url,
        HttpMethod.GET,
        HttpEntity<String>(headers),
        object : ParameterizedTypeReference<List<ExternalApiDto>>() {}
      ).body ?: throw CommonException(CommonExceptionCode.EXTERNAL_API_ERROR)
    } catch (e: Exception) {
      throw CommonException(CommonExceptionCode.EXTERNAL_API_ERROR)
    }
  }
}