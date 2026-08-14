package com.runpassport.runpassportapi.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private const val BEARER_SCHEME_NAME = "bearerAuth"

@Configuration
class SwaggerConfig {

    @Bean
    fun openApi(): OpenAPI {
        val bearerScheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")

        return OpenAPI()
            .info(
                Info()
                    .title("RunPassport API")
                    .version("v1")
                    .description("GPS 인증 러닝 코스 스탬프 투어 앱 RunPassport 백엔드 API 문서")
            )
            .components(Components().addSecuritySchemes(BEARER_SCHEME_NAME, bearerScheme))
            .addSecurityItem(SecurityRequirement().addList(BEARER_SCHEME_NAME))
    }
}
