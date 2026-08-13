package com.runpassport.runpassportapi.common.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.net.http.HttpConnectTimeoutException

class LoggingClientHttpRequestInterceptor : ClientHttpRequestInterceptor {

    private val log = LoggerFactory.getLogger(javaClass)

    // 요청 URI에 등장하는 민감 파라미터, 다른 민감 파라미터 추가시 아래에 추가
    private val sensitiveParamNames = listOf("serviceKey", "apiKey", "api_key", "key")

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val maskedUri = maskSensitiveQueryParams(request.uri.toString())
        log.info("[HTTP 요청] {} {}", request.method, maskedUri)

        val startedAt = System.currentTimeMillis()
        try {
            val response = execution.execute(request, body)
            log.info("[HTTP 응답] {} {} -> status={}", request.method, maskedUri, response.statusCode)
            return response
        } catch (e: HttpConnectTimeoutException) {
            val elapsedMs = System.currentTimeMillis() - startedAt
            log.warn("[HTTP 타임아웃] {} {} -> {}ms 경과 후 실패", request.method, maskedUri, elapsedMs)
            throw e
        }
    }

    /** URI 쿼리스트링에서 serviceKey 같은 민감 파라미터 내용 마스킹 */
    private fun maskSensitiveQueryParams(uri: String): String {
        var masked = uri
        for (paramName in sensitiveParamNames) {
            masked = masked.replace(
                Regex("(?i)([?&]$paramName=)[^&]*"),
                "$1****"
            )
        }
        return masked
    }
}