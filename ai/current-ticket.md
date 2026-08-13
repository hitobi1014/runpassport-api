# [BE] Swagger/OpenAPI 공통 설정 구성

> 상태: IN_PROGRESS
> 시작일: 2026-08-13

---

## 목표

SpringDoc OpenAPI로 API 문서를 자동 생성해서, `/swagger-ui/index.html`에서 지금까지 만든
엔드포인트를 눈으로 확인하고 테스트할 수 있게 한다. `springdoc-openapi-starter-webmvc-ui:2.8.8`은
이미 `build.gradle.kts`에 추가되어 있음 — 별도 의존성 추가는 필요 없다.

---

## 왜 필요한가 (현재 상태)

지금은 컨트롤러가 생겨도 문서화가 전혀 안 되고 있어서, `curl`로 직접 두들겨보는 것 말고는 API 스펙을
확인할 방법이 없다. SpringDoc은 별도 설정 없이도 `/v3/api-docs`, `/swagger-ui/index.html`을 자동으로
띄워주긴 하는데, 지금 프로젝트는 두 가지가 걸린다:

1. **`SecurityConfig`가 기본적으로 모든 요청을 인증 필요로 막고 있다**
   (`anyRequest().authenticated()`). Swagger 관련 경로를 `permitAll()`에 추가하지 않으면
   Swagger UI 자체가 401로 안 열린다 — 지난 `CommonResponse` 티켓에서 `/test/**`를 빼먹었다가
   똑같은 문제를 겪었던 것과 같은 패턴이니 이번엔 처음부터 챙길 것.
2. **아직 실제 JWT 인증 필터가 구현돼 있지 않다** (`jjwt-*` 의존성만 추가된 상태, 실제 토큰 발급/검증
   로직은 없음). 이번 티켓의 "JWT 인증 스키마 설정"은 실제 인증 로직을 만드는 게 아니라, Swagger UI에
   **"이 API는 Bearer 토큰이 필요하다"는 문서 메타데이터(SecurityScheme)만 등록**하는 것이다 —
   나중에 실제 로그인/토큰 발급 기능이 붙었을 때 Swagger UI의 "Authorize" 버튼에 토큰을 넣고 바로
   테스트할 수 있게 미리 준비해두는 개념으로 보면 된다.

---

## 설계 고려사항

### SwaggerConfig에서 할 일은 두 가지 뿐

1. `OpenAPI` 빈 하나 등록 — API 제목/버전/설명 같은 메타데이터 + `SecurityScheme`(HTTP Bearer,
   `bearerFormat: JWT`) 정의
2. `SecurityConfig`에 Swagger 관련 경로를 `permitAll()`로 추가
   (`/swagger-ui/**`, `/v3/api-docs/**` — 정확한 경로 패턴은 SpringDoc 2.8.8 문서 기준으로 직접 확인)

### SecurityScheme을 어떻게 전역 적용할지

`OpenAPI` 빈에 `SecurityScheme`을 컴포넌트로 등록하는 것과, 그걸 실제로 "모든 API에 적용되는
기본 요구사항"으로 걸어주는 `SecurityRequirement`는 별개다. 후자를 빼먹으면 스키마 정의만 있고
Swagger UI에 자물쇠 아이콘이 안 뜨는 경우가 있으니, 등록 후 UI에서 실제로 자물쇠가 보이는지까지
확인할 것.

---

## 작업 순서 (직접 채워나갈 것)

1. `common/config` 패키지에 `SwaggerConfig`(`@Configuration`) 작성
2. `OpenAPI` 빈에 `Info`(title/version/description)와 `SecurityScheme`(Bearer/JWT) 등록
3. `SecurityConfig`의 `permitAll()` 목록에 Swagger 관련 경로 추가
4. 앱 실행 후 `http://localhost:8080/swagger-ui/index.html` 접속해서:
   - 정상적으로 열리는지 (401 안 뜨는지)
   - 지금까지 만든 컨트롤러(엔드포인트)가 목록에 뜨는지
   - "Authorize" 버튼 눌렀을 때 Bearer 토큰 입력창이 뜨는지 확인

---

## 스켈레톤 (시그니처만 — 본문은 직접 채우기)

```kotlin
package com.runpassport.runpassportapi.common.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
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
```

```kotlin
// SecurityConfig.kt의 permitAll() 목록에 추가할 부분 (정확한 경로 패턴은 SpringDoc 2.8.8 기준 확인)
// .requestMatchers(
//     "/actuator/**",
//     "/swagger-ui/**",
//     "/v3/api-docs/**",
// ).permitAll()
```

---

## 완료 조건 체크리스트

- [ ] `SwaggerConfig` 클래스 작성 (`@Configuration`)
- [ ] API 정보 (제목, 버전, 설명) 설정
- [ ] JWT 인증 스키마 설정 (`SecurityScheme`)
- [ ] `/swagger-ui/index.html` 접속 확인 (401 없이 열리고, Authorize 버튼에 Bearer 입력창이 뜸)

---

## 참고 사항

- SpringDoc 버전은 `2.8.8`로 이미 고정돼 있음 — 버전마다 기본 경로가 조금씩 다를 수 있으니 실제
  접속 경로는 직접 확인해서 맞출 것 (`/swagger-ui.html`이 `/swagger-ui/index.html`로 리다이렉트되는
  버전도 있고 아닌 버전도 있음).
- 실제 JWT 토큰 발급/검증 로직은 이 티켓 범위 밖 (다음에 별도 티켓으로 나올 가능성이 높음) — 여기서는
  Swagger 문서용 스키마 등록까지만.
