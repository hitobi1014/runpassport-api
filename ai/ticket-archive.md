# ticket-archive.md

> `develop-ticket.md` 진행 규칙상 DONE된 티켓은 목록에서 삭제된다. 삭제되기 전 완료 메모(배운 점,
> 의사결정, 스코프 변경 사유)를 여기 시간순으로 남긴다.
> BE+INFRA 전용 — Android 쪽 완료 기록은 해당 저장소(runpassport-android)에 따로 관리.
> Notion "Run패스포트 티켓 아카이브" DB에도 같은 내용을 짧게 남기고 원본 Notion 티켓과 관계형으로
> 연결해둔다 (`레포=runpassport-api`로 필터). 여기는 그 로컬 원본(전문).

---

## 2026-08-18 — (#10 하위) regions 마스터 데이터 시딩

- **Notion**: [(#10 하위) regions 마스터 데이터 시딩](https://app.notion.com/p/3c09c9e2d478810d8c55f2c41ef6585e) (Run패스포트 티켓 아카이브 DB), 상위 티켓 [#10 코스 원본 데이터 실제 적재](https://app.notion.com/3b49c9e2d4788171a428c3421b375301)
- **목표**: `courses.region_id` NOT NULL FK가 요구하는 `regions` 테이블에 데이터를 채운다.
- **완료 메모**:
    - 스코프 변경: raw_durunubi 실제 데이터 기준 57개 시군구 전체 시딩 대신, `courses.region_id` FK가
      정상 동작하는지 확인하는 선에서 마무리하기로 함. 지금 `regions`에 있는 3건(서울/일산/거제)은 이
      확인용 테스트 데이터.
    - 시연용 지역 데이터는 별도 후속 작업: 어떤 코스를 시연할지 정해지면, 그 코스가 속한 지역만 몇 건
      추가하는 식으로 진행 — raw_durunubi 건수(57개)를 그대로 따라가지 않기로 함.
    - 스키마: `regions` 테이블은 `id, name, sigun, center_lat, center_lng` 컬럼만 가짐. 원래
      `sido`/`sigungu` 구조화 컬럼도 유지할 계획이었으나, 실제 `20260817065314_add_sigun_to_regions.sql`
      마이그레이션에서 drop되고 원본 두루누비 `sigun` 표기 문자열 단일 컬럼만 남음 — 이후 `regions`
      참고 시 이 스키마 기준으로 볼 것.
- **관련 파일**: `supabase/migrations/20260817065314_add_sigun_to_regions.sql`
