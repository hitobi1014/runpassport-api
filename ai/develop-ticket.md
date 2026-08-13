# develop-ticket.md

> 이 파일은 프로젝트 진행 상황의 단일 진실 공급원 (source of truth)입니다.
> Claude Code는 새 작업을 시작하기 전에 이 파일을 먼저 읽습니다.
> 관리 규칙은 `CLAUDE.md` 참고.

---

## 현재 상태 요약

- **마지막 업데이트**: 2026-08-13
- **지금 진행 중**: Swagger/OpenAPI 공통 설정 구성
- **다음 할 일**: 두루누비 routeList (길 목록) 연동 여부 검토
- **막힌 것**: 없음

---

## 진행 규칙 (요약, 상세는 CLAUDE.md 참고)

1. 종료 (DONE)된 티켓 항목이 존재할 경우 해당 티켓은 삭제
2. 티켓 하나 = 눈으로 확인 가능한 완료 조건 단위
3. 개발 → 검증 (실행/로그 확인) → 통과 → 상태 갱신 → 다음 티켓
4. 앞 티켓이 DONE 되기 전 다음 티켓 코드 작성 금지
5. 코드는 사용자가 직접 작성, Claude Code는 설계/리뷰만 (예외: 단순 보일러플레이트)
6. 현재 진행중인 티켓에 대한 상세내용 (작업 순서, 예시 등)은 같은 경로 current-ticket.md에 작성

---

## 티켓 목록

### [BE] 전역 예외 처리 구성

- **상태**: DONE
- **작업 내용**: @ControllerAdvice 기반 전역 예외 핸들러 + 커스텀 예외 클래스 정의
- **완료 조건**:
    - [x] `GlobalExceptionHandler` 클래스 작성 (@RestControllerAdvice)
    - [x] 비즈니스 예외용 `BusinessException` open class 정의
    - [x] 공통 에러 응답 DTO 정의 (`ErrorResponse`)
    - [x] 의도적으로 예외 발생시켜 핸들러 동작 확인
- **완료 메모**: HTTP 상태코드를 ErrorCode.status에서 가져오도록 구현. Validation 에러는 첫 번째 필드 에러만 추출.

---

### [BE] API 공통 응답 래퍼 구성

- **상태**: DONE
- **작업 내용**: 성공/실패 응답을 일관된 구조로 감싸는 CommonResponse<T> 정의
- **완료 조건**:
    - [x] `CommonResponse<T>` data class 작성 (`data`, `message` 필드)
    - [x] 성공 응답 팩토리 메서드 (`CommonResponse.success(data)`)
    - [x] 테스트 컨트롤러에서 래퍼 적용 후 JSON 응답 확인
- **완료 메모**: 설계 단계에서 Option B (성공만 래핑, 에러는 기존 `ErrorResponse` +
  `GlobalExceptionHandler` 재사용)로 결정하면서 "실패 응답 팩토리 메서드" 조건은 빠짐 — 불필요한 null 필드를 안 만들기 위한 의도적 선택. 검증용으로 만든 `TestController`
  는 코드리뷰 반영하면서 삭제 (＋`SecurityConfig`의 `/test/**` permitAll도 함께 정리). 코드리뷰:
  `ai/code-review/api-common-response-wrapper_2026-08-13.md`.

---

### [BE] RestClient 공통 설정 구성

- **상태**: DONE
- **작업 내용**: 외부 API 호출용 RestClient Bean 설정 (타임아웃, 로깅 등)
- **완료 조건**:
    - [x] 공통 RestClient 설정 컴포넌트 작성 (당초 계획한 `RestClientConfig`(@Configuration) 대신
      `LoggingRestClientCustomizer`(`RestClientCustomizer` 구현)로 완성 — 아래 완료 메모 참고)
    - [x] 커넥션/읽기 타임아웃 설정 (`spring.http.clients.connect-timeout`/`read-timeout`,
      `application-durunubi-real.yml`에서 주입)
    - [x] 요청/응답 로깅 인터셉터 추가 (`LoggingClientHttpRequestInterceptor`, 민감 파라미터 마스킹 포함)
    - [x] 기존 RealDurunubiCourseFetcher에서 공통 RestClient 사용하도록 수정
    - [x] 두루누비 배치 실행해서 정상 동작 확인 (성공/타임아웃 실패 케이스 둘 다 실행 로그로 검증)
- **완료 메모**: 처음엔 `@Bean fun restClientBuilder(): RestClient.Builder = RestClient.builder()...`
  형태로 직접 새 빌더를 만들었는데, 이러면 Boot가 `spring.http.clients.*` 프로퍼티로 자동 설정해주는 타임아웃이 통째로 무시된다는 걸 발견함
  (`@ConditionalOnMissingBean`이라 Boot 자동설정이 스킵됨).
  `RestClientCustomizer`를 구현하는 컴포넌트로 바꿔서, Boot가 이미 만들어둔 빌더에 인터셉터만 얹는 방식으로 해결 — 커스텀 `RestClient.Builder` 빈을 새로 정의하지 말고
  `RestClientCustomizer`를 쓸 것. 코드리뷰: `ai/code-review/restclient-common-config_2026-08-13.md`,
  `ai/code-review/restclient-common-config_2026-08-13_v2.md`.

---

### [BE] Swagger/OpenAPI 공통 설정 구성

- **상태**: IN_PROGRESS
- **작업 내용**: SpringDoc OpenAPI 설정으로 API 문서 자동 생성
- **완료 조건**:
    - [ ] `SwaggerConfig` 클래스 작성 (@Configuration)
    - [ ] API 정보 (제목, 버전, 설명) 설정
    - [ ] JWT 인증 스키마 설정 (SecurityScheme)
    - [ ] `/swagger-ui.html` 또는 `/swagger-ui/index.html` 접속 확인
- **완료 메모**: (아직 없음)

---

### [BE] 두루누비 routeList (길 목록) 연동 여부 검토

- **상태**: TODO
- **작업 내용**: 사용할지 결정 안 됨. 쓴다면 원천DB 저장 방식 설계 필요
- **완료 조건**:
    - [ ] courseList만으로 충분한지 판단 (routeIdx 필드로 참조만 하고 별도 수집은 안 해도 되는지)
    - [ ] 쓰기로 결정되면 RAW_ROUTE 테이블 스키마 설계
- **완료 메모**: (아직 없음)

---

### [BE] 관광공사 TourAPI 원본 데이터 배치 스크립트 작성

- **상태**: TODO
- **작업 내용**: RAW_TOUR 테이블에 관광공사 데이터 적재. 두루누비 배치 패턴 재사용
- **완료 조건**:
    - [ ] 두루누비와 같은 Fetcher 인터페이스 패턴으로 구현
    - [ ] Mock 흐름 검증
    - [ ] 실제 API 연동
- **완료 메모**: (아직 없음)

---

<!--
새 티켓 추가 템플릿 (복사해서 사용):

### [BE|AND|INFRA] 동사로 시작하는 티켓명

- **상태**: TODO
- **작업 내용**:
- **완료 조건**:
  - [ ]
- **완료 메모**:
- **작업 순서**:
-->