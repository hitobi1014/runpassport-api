# develop-ticket.md

> 이 파일은 프로젝트 진행 상황의 단일 진실 공급원 (source of truth)입니다.
> Claude Code는 새 작업을 시작하기 전에 이 파일을 먼저 읽습니다.
> 관리 규칙은 `CLAUDE.md` 참고.

---

## 현재 상태 요약

- **마지막 업데이트**: 2026-08-17
- **지금 진행 중**: regions 마스터 데이터 시딩
- **다음 할 일**: raw_durunubi → courses 변환 배치
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

> DONE 처리된 티켓은 목록에서 삭제한다 (진행 규칙 1). 과거 완료 내역/코드리뷰는
> `ai/code-review/`에 날짜별로 남아있음:
> `전역 예외 처리 구성`, `api-common-response-wrapper_2026-08-13`,
> `restclient-common-config_2026-08-13(+_v2)`, `swagger-openapi-config_2026-08-13(+_14)`.
> "두루누비 routeList 연동 여부 검토"는 A안(연동 안 함)으로 결론 — routeIdx는 raw_durunubi에
> 이미 있으니 별도 수집/스키마 없이 필요할 때 참조만 하기로 함. 근거는
> `ai/current-ticket.md` 히스토리(2026-08-14) 참고.
> "관광공사 TourAPI 원본 데이터 배치 스크립트 작성"은 DONE — Mock은 의도적으로 스킵(실 데이터만
> 적재하면 되는 파이프라인이라 불필요 판단)하고 바로 실제 API 연동으로 감. `RealTourContentFetcher`
> + `TourContentIngestService`/`TourContentBatchRunner`로 두루누비 패턴 재사용, `raw_tour` 실제
> 49건 적재까지 검증. 코드리뷰: `ai/code-review/tourapi-batch_2026-08-16.md`,
> `ai/code-review/tourapi-batch_2026-08-17.md` (params 컬럼에 serviceKey 안 새는지 등 검증).

### [BE] regions 마스터 데이터 시딩

- **상태**: IN_PROGRESS
- **작업 내용**: `courses.region_id` FK가 요구하는 `regions` 테이블 시딩. `raw_durunubi` 실제
  데이터 기준 11개 시도/57개 시군구가 우선 필요 범위 (상세는 `current-ticket.md`)
- **완료 조건**:
    - [ ] 좌표 소스 결정
    - [ ] `regions`에 57개 시군구 데이터 적재
    - [ ] 적재 확인
- **완료 메모**: (아직 없음)

---

### [BE] raw_durunubi → courses 변환 배치

- **상태**: TODO
- **작업 내용**: 원본(raw_durunubi) 데이터를 실제 서비스 테이블(courses)로 옮기는 배치.
  regions 시딩이 선행되어야 함 (region_id FK)
- **완료 조건**:
    - [ ] (regions 시딩 완료 후 current-ticket.md에서 상세 설계)
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