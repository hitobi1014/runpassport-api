# develop-ticket.md

> 이 파일은 프로젝트 진행 상황의 단일 진실 공급원(source of truth)입니다.
> Claude Code는 새 작업을 시작하기 전에 이 파일을 먼저 읽습니다.
> 관리 규칙은 `CLAUDE.md` 참고.

---

## 현재 상태 요약

- **마지막 업데이트**: (여기 날짜 채우기)
- **지금 진행 중**: 두루누비 courseList 원본 데이터 배치 스크립트 (Mock 흐름 검증 단계)
- **다음 할 일**: 실제 API 연동(`durunubi-real` 프로파일) 테스트
- **막힌 것**: 없음

---

## 진행 규칙 (요약, 상세는 CLAUDE.md 참고)

1. 티켓 하나 = 눈으로 확인 가능한 완료 조건 단위
2. 개발 → 검증(실행/로그 확인) → 통과 → 상태 갱신 → 다음 티켓
3. 앞 티켓이 DONE 되기 전 다음 티켓 코드 작성 금지
4. 코드는 사용자가 직접 작성, Claude Code는 설계/리뷰만 (예외: 단순 보일러플레이트)

---

## 티켓 목록

### [INFRA] 배치 실행용 프로파일 구조 설정

- **상태**: DONE
- **완료 조건**:
    - [x] `batch-durunubi` 프로파일 시 `web-application-type: none`으로 웹서버 안 뜸
    - [x] `durunubi-real` 프로파일 시 `durunubi.api.*` 설정값 주입됨
    - [x] `application-batch-durunubi.yml`, `application-durunubi-real.yml`로 설정 분리
- **완료 메모**:
    - 두 프로파일의 설정 관심사가 달라서(웹서버 여부 vs API 키) 한 파일에 합치면 안 됨 —
      Mock 배치만 돌릴 때도 웹서버가 꺼져야 하는데 합쳐두면 그게 깨짐
    - 비밀값은 `.env.local` + `spring.config.import=optional:file:.env.local[.properties]`
      조합으로 플러그인 없이 로드

---

### [BE] 두루누비 courseList 원본 데이터 배치 스크립트 작성

- **상태**: DONE
- **작업 내용**: RAW_DURUNUBI 테이블에 두루누비 코스목록 데이터 적재
- **완료 조건**:
    - [x] `DurunubiCourseFetcher` 인터페이스로 Mock/Real 분리
    - [x] Mock으로 실행 시 목업 데이터가 DB에 적재됨
    - [x] 실제 API 응답 구조에 맞춰 파싱 로직 수정 (`response` 래핑 반영)
    - [x] delete-all-then-insert(delSert) 방식으로 저장 로직 수정, 빈 응답일 때 삭제 안 하도록 보호
    - [x] `durunubi-real` 프로파일로 실제 API 연동 테스트
- **완료 메모**:
    - 실제 응답이 문서 스펙과 다르게 최상위가 `response`로 한 번 더 감싸져 있었음
      (`responseBody.path("response").path("header")...`) → `api-notes.md`에 기록 권장
    - `service-key` 미설정 시 `@PostConstruct`에서 fail-fast 하도록 처리함

---

### [BE] 두루누비 routeList(길 목록) 연동 여부 검토

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
-->