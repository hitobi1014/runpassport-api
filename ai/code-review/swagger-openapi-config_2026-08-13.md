# 코드 리뷰: [BE] Swagger/OpenAPI 공통 설정 구성

- 리뷰 일시: 2026-08-13
- 대상 변경: 워킹트리 변경사항 (`git diff HEAD`, 커밋 전)
- 변경 파일:
  - `src/main/kotlin/com/runpassport/runpassportapi/common/config/SwaggerConfig.kt` (신규)
  - `src/main/kotlin/com/runpassport/runpassportapi/test/TestController.kt` (신규)
  - `src/main/kotlin/com/runpassport/runpassportapi/sample/SwaggerDTOSample.kt` (신규)
  - `src/main/kotlin/com/runpassport/runpassportapi/auth/SecurityConfig.kt` (수정)

## 완료 조건 대조

`ai/current-ticket.md` 기준 4개 전부 확인해봤는데, **현재 상태로는 앱이 아예 기동이 안 돼서 4개 다
미충족**이다. 아래 🔴가 원인.

- [ ] `SwaggerConfig` 클래스 작성 (`@Configuration`) — 파일/애노테이션은 있지만
  `openApi()` 본문이 `TODO()` 그대로라 사실상 미구현.
- [ ] API 정보(제목/버전/설명) 설정 — 미구현.
- [ ] JWT 인증 스키마 설정(`SecurityScheme`) — 미구현. `TestController.login()`에 붙인
  `@SecurityRequirements`(빈 값으로 "이 엔드포인트는 예외")는 좋은 시도인데, 애초에 전역
  `SecurityScheme`/`SecurityRequirement`가 등록이 안 돼 있어서 지금은 아무 효과가 없다.
- [ ] `/swagger-ui/index.html` 접속 확인 — **직접 실행해봤는데 애플리케이션 컨텍스트 자체가
  기동에 실패해서 접속 자체가 불가능**. 아래 🔴 참고.

## 발견한 이슈

### 🔴 반드시 확인 (버그·로직 오류)

- **`SwaggerConfig.kt:13` — `openApi()`가 `TODO()`를 그대로 반환해서 앱이 부팅되지 않는다.**
  `@Bean`은 스프링이 컨텍스트를 띄울 때 즉시(eager) 생성하는 게 기본이라, 이 메서드가 호출되는
  순간 `TODO()`가 `kotlin.NotImplementedError`를 던지고 그대로 컨텍스트 초기화가 실패한다.
  직접 `./gradlew bootRun`으로 실행해서 재현했다:
  ```
  BeanCreationException: Error creating bean with name 'openApi' ...
  Caused by: kotlin.NotImplementedError: An operation is not implemented.
      at com.runpassport.runpassportapi.common.config.SwaggerConfig.openApi(SwaggerConfig.kt:14)
  ```
  즉 지금 상태의 코드는 Swagger 뿐 아니라 **애플리케이션 전체가 안 뜬다.** `current-ticket.md`의
  스켈레톤에 있던 `TODO()`를 실제 `OpenAPI` 객체 생성 코드로 채우는 게 이 티켓의 핵심 작업인데,
  그 부분이 아직 안 채워진 채로 주변 코드(SecurityConfig, TestController, 샘플 DTO)만 먼저
  작성된 상태로 보인다. 커밋 전에 반드시 채워야 한다 — `Info`, `SecurityScheme` 등록까지 끝나야
  나머지 완료 조건(스키마 설정, UI 접속 확인)도 같이 검증할 수 있다.

### 🟡 개선 제안 (컨벤션·가독성·코틀린다움)

- `TestController.kt:23` — `@GetMapping()`처럼 빈 괄호를 쓰고 있는데, 인자가 없을 땐
  `@GetMapping`으로 괄호 자체를 생략하는 게 코틀린/스프링에서 더 흔한 스타일이다. 사소하지만
  같은 파일 안에서 `@PostMapping("/login")`처럼 인자 있는 것과 없는 것이 섞여 있으니 통일하면 좋겠다.
- `SwaggerDTOSample.kt` — 지금 어디서도 참조되지 않는 죽은 코드다. 파일 상단 주석에 적어둔
  "`@field:Schema`를 안 쓰면 코틀린 data class에서 Swagger가 필드를 못 읽는다"는 내용 자체는
  좋은 학습 메모인데, 실제 소스 파일로 남겨두려면 최소한 `TestController`에 이 DTO를 반환하는
  엔드포인트 하나 정도는 붙여서 "실제로 swagger-ui에 필드 설명/예시가 잘 뜨는지"까지 검증에 쓰는 게
  낫겠다. 그게 아니라 순수 학습 메모라면 `CLAUDE.md`가 제안하는 `kotlin-notes.md`에 옮기고 이
  파일은 지우는 것도 방법이다 (검증에도 안 쓰이고 실행되지도 않는 소스 파일은 나중에 "이거 왜
  있지" 하고 헷갈리게 만들기 쉽다).
- `TestController.kt` 맨 아래 `LoginRequest` data class가 컨트롤러 클래스 밖, 파일 최하단에
  같이 들어있다. 코틀린에서 파일 하나에 여러 top-level 선언을 두는 것 자체는 문제없지만, 다른
  기존 코드(`RawDurunubi`/`RawDurunubiRepository`처럼 클래스당 파일 하나)와 비교하면 살짝
  결이 다르다. 지금은 임시 검증용 컨트롤러라 크게 문제 삼을 정도는 아니다.

### 🟢 잘한 점

- `TestController`를 이번엔 루트 패키지가 아니라 `test` 하위 패키지에 둔 것 — 지난
  `CommonResponse` 티켓 코드리뷰에서 지적했던 "패키지가 루트에 그대로 있다"는 피드백을 이번엔
  반영했다.
- `SecurityConfig`에 `/swagger-ui/**`, `/v3/api-docs/**`를 `permitAll()`로 추가한 것 —
  `current-ticket.md`에 미리 적어둔 "빼먹기 쉬운 부분"을 놓치지 않고 챙겼다.
- `login()` 엔드포인트에 `@SecurityRequirements`(빈 값)를 붙여서 "이 엔드포인트는 전역 인증
  요구사항에서 제외"라는 의도를 표현한 것 — 지금은 전역 요구사항 자체가 없어서 효과가 없지만,
  SpringDoc에서 개별 엔드포인트를 전역 보안 정책에서 빼는 올바른 패턴을 이미 알고 쓴 것으로 보인다.
  `SwaggerConfig`만 마저 채우면 의도대로 동작할 것.

## 이번 티켓과 별개로 발견한 것 (참고용)

이번 리뷰 범위는 아니지만, 워킹트리를 살펴보다가 이미 **커밋된 상태**(`380b29f`)인
`application-durunubi-real.yml`에서 회귀로 보이는 부분을 발견해서 같이 남겨둔다.

```yaml
durunubi:
  api:
    ...

  spring:              # <- 들여쓰기가 durunubi: 밑에 그대로 남아있음
    http:
      clients:
        connect-timeout: 5s
        read-timeout: 5s
```

`spring:`이 `durunubi:`와 같은 들여쓰기 레벨(2칸)이 아니라 그 **밑에 중첩**돼 있다. 이러면 실제
프로퍼티 키가 `spring.http.clients.connect-timeout`이 아니라 `durunubi.spring.http.clients.connect-timeout`이 되어버려서,
바로 전 티켓(`RestClient 공통 설정 구성`)에서 실행까지 해서 어렵게 검증했던 타임아웃 설정이
다시 무효화됐을 가능성이 높다. `spring:`을 파일 최상위 레벨(들여쓰기 0칸)로 옮겨야
`durunubi:`와 별개의 최상위 키로 인식된다. 이번 Swagger 티켓 완료 후 `SwaggerConfig` 버그부터
고쳐서 앱이 뜨게 만든 다음, 이 부분도 같이 확인해보길 권한다 (지금은 앱 자체가 안 떠서 실제로
재현 테스트는 못 해봤다 — 파일 내용만 보고 판단한 것이니 직접 한번 확인해볼 것).

## 다음 액션

1. **`SwaggerConfig.openApi()`의 `TODO()`를 실제 구현으로 채우는 게 최우선.** 이거 없이는 앱 자체가
   안 뜬다.
2. 구현 후 `/swagger-ui/index.html` 접속해서: 401 없이 열리는지, `TestController`의 두 엔드포인트가
   목록에 뜨는지, Authorize 버튼에 Bearer 입력창이 뜨는지, `login`은 자물쇠 아이콘이 안 뜨고 나머지는
   뜨는지까지 확인
3. `SwaggerDTOSample.kt`를 실제로 쓸지, 학습 노트로 옮기고 지울지 결정
4. (참고용) `application-durunubi-real.yml`의 `spring:` 들여쓰기 회귀 — 시간 될 때 확인
5. 완료 조건이 모두 충족되면: `develop-ticket.md` 상태를 `DONE`으로 갱신하고, 다음 티켓
   "두루누비 routeList 연동 여부 검토"로 넘어가면 됨
