# [BE] 관광공사 TourAPI 원본 데이터 배치 스크립트 작성

> 상태: IN_PROGRESS
> 시작일: 2026-08-14

---

## 목표

`raw_tour` 테이블에 관광공사 TourAPI 응답을 적재하는 배치를 만든다. 두루누비 때 만든 패턴 (Fetcher 인터페이스로 Mock/Real 분리, delete-all-then-insert, 방어적
파싱)을 그대로 재사용한다.

---

## 이미 가진 것 / 재사용할 것

- **`LoggingRestClientCustomizer`(`common/config`)는 전역으로 이미 적용돼 있음** — TourAPI용 Fetcher를 만들 때 RestClient 관련 설정을 새로 할 필요가
  없다. `RestClient.Builder`를 주입받아서
  `.baseUrl()`만 붙이면 로깅/타임아웃이 자동으로 따라온다 (`RealDurunubiCourseFetcher`가 하던 것과 똑같이).
- **delete-all-then-insert 패턴** — 두루누비 때 확정한 방식 (전체 삭제 후 재삽입, API 실패/빈 응답이면 삭제 안 함, `@Transactional`로 묶기)을 그대로 따른다.
  `DurunubiCourseIngestService`가 참고용 예시.
- **item 배열/단일객체 방어 파싱** — 공공데이터 API 흔한 패턴이니 TourAPI도 똑같이 대비해둘 것 (`RealDurunubiCourseFetcher.extractItems()` 참고).

---

## 두루누비와 다른 점 (설계 시 고려할 것)

### `raw_tour`는 복합 유니크 키를 쓴다

```sql
create table public.raw_tour
(
    id              bigint generated always as identity primary key,
    external_id     text        not null,
    content_type_id integer     not null,
    raw_payload     jsonb       not null,
    collected_at    timestamptz not null default now(),
    constraint raw_tour_external_id_content_type_id_key unique (external_id, content_type_id)
);
```

`raw_durunubi`는 `external_id` 하나가 유니크였지만, 여기는 `(external_id, content_type_id)` 조합이 유니크다 — TourAPI는 관광타입
(`content_type_id`: 39 음식점, 38 쇼핑 등)별로 같은 장소가 다른 타입으로도 잡힐 수 있다는 뜻으로 보인다. Fetcher가 **여러 `content_type_id`를 순회하면서 각각 호출**
해야 할 가능성이 높다 — 두루누비처럼 단일 엔드포인트 페이징 하나로 끝나지 않을 수 있다는 걸 염두에 두고 설계할 것.

### 실제 TourAPI 스펙은 아직 확인 안 됨

이번 세션에서 TourAPI 키를 발급받았거나 실제로 호출해본 적이 없다. 두루누비도 처음엔 문서 스펙과 실제 응답이 달랐던 적이 있었으니 (`response`로 한 번 더 감싸져 있었던 것), TourAPI도
마찬가지로 가정하지 말고 실제 발급받은 키 문서 + 실제 호출 결과로 확인할 것. 어떤 오퍼레이션 (지역기반 목록조회 등 이름은 실제 문서 기준으로 확인)을 쓸지도 아직 미정.

---

## 작업 순서 (직접 채워나갈 것)

1. ~~`ingest.tour` 패키지 생성 (durunubi와 대칭 구조)~~
2. ~~`RawTour`(엔티티), `RawTourRepository` 작성 — `RawDurunubi`/`RawDurunubiRepository` 그대로 참고해서 만들면 됨 (복합 유니크 키 반영)~~
3. ~~`TourContentFetcher` 인터페이스 + DTO 정의 (`externalId`, `contentTypeId`, `rawPayload`)~~
4. ~~`MockTourContentFetcher` 작성 — 실제 스펙 몰라도 일단 그럴듯한 목업 필드로 몇 건 만들어서 먼저 Mock 흐름부터 검증 (완료 조건 2번)~~
5. `TourContentIngestService`, `TourContentBatchRunner` — `DurunubiCourseIngestService`/
   `DurunubiCourseBatchRunner` 그대로 패턴 재사용
6. Mock으로 `raw_tour` 적재까지 확인되면, 그다음에 실제 TourAPI 키 받아서 `RealTourContentFetcher`
   구현 (완료 조건 3번 — 이 단계에서 실제 응답 구조 확인 필수)

---

## 스켈레톤 (시그니처만 — 본문은 직접 채우기)

```kotlin
package com.runpassport.runpassportapi.ingest.tour

interface TourContentFetcher {
    fun fetchContents(): List<TourContentItem>
}

data class TourContentItem(
    val externalId: String,
    val contentTypeId: Int,
    val rawPayload: String,
)
```

```kotlin
package com.runpassport.runpassportapi.ingest.tour

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "raw_tour")
class RawTour(
    // TODO: id, externalId, contentTypeId, rawPayload, collectedAt
    //       RawDurunubi.kt 구조 그대로 참고해서 채울 것. unique(external_id, content_type_id)는
    //       DB 제약이 이미 있으니 엔티티에는 굳이 @Table(uniqueConstraints=...) 안 넣어도 됨
    //       (ddl-auto: validate라 스키마는 마이그레이션이 소유).
)
```

```kotlin
package com.runpassport.runpassportapi.ingest.tour

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!tour-real")
class MockTourContentFetcher : TourContentFetcher {
    // TODO: 목업 TourContentItem 몇 건 반환 (외부 스펙 확정 전이니 필드는 대략적으로)
    override fun fetchContents(): List<TourContentItem> = TODO()
}
```

---

## 완료 조건 체크리스트

- [ ] 두루누비와 같은 Fetcher 인터페이스 패턴으로 구현
- [ ] Mock 흐름 검증 (`batch-tour` 같은 프로파일로 실행해서 `raw_tour`에 적재되는 것까지 확인)
- [ ] 실제 API 연동 (TourAPI 키 발급 후 진행 — 아직이면 이 조건은 다음 세션으로 미뤄도 됨)

---

## 참고 사항

- 티켓 범위가 좀 큰 편이다 (Mock 검증 + 실제 연동이 한 티켓에 같이 있음). 진행하다가 Mock까지만 먼저 끝내고 "여기까지 됐다" 확인받은 다음 실제 연동을 별도로 이어가는 것도 방법 —
  CLAUDE.md의 "티켓 하나 = 눈으로 확인 가능한 단위로 쪼개기" 원칙에 맞으면 중간에 쪼개도 된다.
- TourAPI 키가 아직 없으면 3번 완료 조건은 자연히 막히는 (BLOCKED) 상태가 된다 — 그 경우
  `develop-ticket.md`에 왜 막혔는지 남겨둘 것.
