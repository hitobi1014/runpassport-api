package com.runpassport.runpassportapi.common.config

import io.swagger.v3.oas.models.OpenAPI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    // TODO: Info(title, version, description)와 SecurityScheme(HTTP Bearer, bearerFormat="JWT")를
    //       담은 OpenAPI 빈을 만들어서 반환. SecurityScheme을 등록만 하고 실제 요구사항으로
    //       걸어주는 걸 빼먹지 않았는지 UI에서 확인할 것 (설계 고려사항 참고).
    @Bean
    fun openApi(): OpenAPI {
        TODO()
    }
}