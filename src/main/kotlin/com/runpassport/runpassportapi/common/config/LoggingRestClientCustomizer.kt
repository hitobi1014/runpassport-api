package com.runpassport.runpassportapi.common.config

import org.springframework.boot.restclient.RestClientCustomizer
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * 스프링 부트가 spring.http.clients.* 프로퍼티(connect-timeout, read-timeout 등)로
 * 이미 자동 설정해둔 RestClient.Builder에, 로깅 인터셉터만 "추가"로 얹는다.
 *
 * 주의: 여기서 RestClient.builder()로 새 빌더를 직접 만들면 안 됨.
 * 그렇게 하면 스프링 부트의 자동 설정(@ConditionalOnMissingBean)이 통째로 무력화되고,
 * spring.http.clients.* 에 설정한 타임아웃 값이 전부 무시된다.
 * RestClientCustomizer는 자동 설정이 끝난 기존 빌더를 그대로 두고 콜백으로만 건드리므로 안전함.
 */
@Component
class LoggingRestClientCustomizer : RestClientCustomizer {
    override fun customize(restClientBuilder: RestClient.Builder) {
        restClientBuilder.requestInterceptor(LoggingClientHttpRequestInterceptor())
    }
}