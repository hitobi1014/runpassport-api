package com.runpassport.runpassportapi.test

import com.runpassport.runpassportapi.common.response.CommonResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Test", description = "테스트 API")
@RestController
@RequestMapping("/test")
class TestController {

    @Operation(
        summary = "테스트 기본 api",
        description = "테스트 설명"
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @GetMapping()
    fun testBasicRequest(): CommonResponse<String> {
        return CommonResponse.success(data = "데이터", message = "성공 메시지")
    }

    @Operation(summary = "인증 api 테스트")
    @SecurityRequirements
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): CommonResponse<String> {
        return CommonResponse.success(data = "토큰adsfasfqewr", message = "로그인 성공")
    }

}

data class LoginRequest(
    @field:Schema(description = "로그인id", example = "testId12345")
    val id: String,
    @field:Schema(description = "비밀번호", example = "pasdfasd123")
    val password: String,
)