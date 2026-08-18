# 현재 작업 티켓: [BE] (#10 하위) raw_durunubi → courses 변환 배치

- 노션 링크: 자체 티켓 없음 (상위 티켓 [#10 코스 원본 데이터 실제 적재](https://app.notion.com/3b49c9e2d4788171a428c3421b375301)의 하위 작업)
- 소속 마일스톤: W2 · 코스 탐색 + 러닝지수 MVP 동작
- 담당 영역: 백엔드
- 선행 티켓: regions 마스터 데이터 시딩 — DONE (`ai/ticket-archive.md` 참고, 현재 `regions` 3건: 서울/일산/거제)

---

## 목표

`raw_durunubi`에 적재된 코스 원본 (현재 144건) 중, `regions.sigun`과 매칭되는 지역의 코스만 골라
`courses` 테이블로 변환해 넣는다. 배치를 실행하면 `courses`에 매칭된 건수만큼 행이 생기고, 안 맞는 지역의 코스는 건너뛰고, 재실행해도 중복 insert되지 않는 상태가 목표.

---

## 실제 데이터로 확인한 것 (추측 아님, DB 직접 조회)

- `raw_durunubi`: 144건 (실제 두루누비 API로 적재된 실데이터, 전부 "남파랑길"/"DMZ 평화의 길" 시리즈)
- `regions`: 현재 3건 (서울 강동구 / 경기 고양시 / 경남 거제시) — regions 티켓에서 스코프 축소 결정
- 두 테이블을 `sigun` 기준으로 조인하면 **지금 시점 매칭되는 코스는 7건**:
    - 서울 강동구: 1건 (DMZ 평화의 길 19-1코스)
    - 경기 고양시: 2건 (DMZ 평화의 길 5코스, 4-1코스)
    - 경남 거제시: 4건 (남파랑길 16/18/19/20코스)
    - → 이 7건이 이번 배치의 실질적인 완료 확인 기준. "시연 대상 코스를 먼저 정해야 하나?"를 develop-ticket.md에 적어뒀었는데, 실제로는 region 매칭 자체가 자동으로 범위를 좁혀주므로
      별도로 미리 정할 필요는 없어 보임 (region이 없는 지역 코스는 그냥 스킵됨).
- `raw_payload` 실제 필드 (mock과 다른 부분 있음 — 아래 "발견한 것" 참고):
  ```json
  {
    "sigun": "부산 영도구", "brdDiv": "DNWW", "crsIdx": "T_CRS_MNG0000005118",
    "gpxpath": "https://...gpx", "crsCycle": "비순환형", "crsDstnc": "14",
    "crsKorNm": "남파랑길 3코스", "crsLevel": "2", "routeIdx": "...",
    "crsSummary": "...", "crsContents": "...", "crsTourInfo": "...",
    "crsTotlRqrmHour": "330"
  }
  ```

### 발견한 것 — 문서/Mock 스펙과 실제 응답이 다른 지점

- **`brdDiv`(지형 후보 필드)가 144건 전부 "DNWW" 고정값** — `courses.terrain_type`(강변/해안/공원 등)에 쓸 수 있을 줄 알았는데, 이 데이터셋 (남파랑길/DMZ 평화의 길
  시리즈)에서는 전혀 변별력이 없음. 다른 구조화된 지형 필드가 raw_payload에 없어서, terrain_type을 채울 신뢰할 만한 소스가 현재 없음.
- `courses.gpx_storage_ref` 컬럼 코멘트는 "GPX 파일 참조 (Storage)"인데, Supabase Storage에 GPX를 업로드하는 파이프라인은 아직 없음
  (`note/gpx-tech.md`는 regions 중심좌표 계산 용도로만 gpxpath를 다뤘음, courses용 Storage 업로드는 별개 미착수 작업).

### 코드베이스 확인

- `ingest/course/` 디렉토리가 이미 빈 채로 만들어져 있음 — 이 작업을 여기 넣으라는 자리 표시로 보임.
- `Course`(courses 테이블용 JPA 엔티티), `CourseRepository`가 아직 없음.
- **`Region`(regions 테이블용 JPA 엔티티)도 아직 없음** — `sigun` → `region_id` 조회를 하려면 이것도 필요한데 기존 티켓 범위에 없었던 부분. 이번 티켓에서 최소한으로 같이
  만들어야 할 것으로 보임.
- `courses.raw_durunubi_id`에 unique 제약이 없음 (인덱스만 있음) — DB 레벨에서 중복 변환을 막아주지 않음, 애플리케이션에서 처리해야 함.

---

## 완료 조건 (Definition of Done)

- [x] `Course` 엔티티 + `CourseRepository` 작성 (courses 테이블 매핑)
- [x] `Region` 엔티티 + `RegionRepository` 작성 (regions 테이블 매핑, sigun으로 조회 가능해야 함)
- [ ] 변환 서비스가 `raw_durunubi` 전체를 훑어서, `regions.sigun`과 매칭되는 것만 `courses`로 insert
- [ ] region이 안 맞는 코스 (현재 144-7=137건)는 에러 없이 스킵되고, 스킵 건수가 로그에 남음
- [ ] 이미 변환된 raw_durunubi 건은 재실행 시 중복 insert되지 않음 (멱등성)
- [ ] 배치 실행 후 `select count(*) from courses` 로 7건 (현재 regions 기준) 확인
- [ ] 변환된 행 하나를 골라 raw_payload 원본과 대조해서 필드 매핑이 맞는지 확인 (이름/거리/난이도)

---

## 구현 순서

1. ~~**`Region` 엔티티 + `RegionRepository` 작성** — `regions` 테이블 (`id, name, sigun, center_lat,
   center_lng`) 매핑. `RawDurunubi.kt` 패턴 그대로 (일반 `class`, data class 아님). 위치는
   `region/Region.kt`, `region/RegionRepository.kt` (최상위 도메인 패키지 — `ingest/course/`가 아님, 이유는 `ai/response.md` "패키지 설계
   구조" 참고: regions는 courses 말고도 여러 테이블이 참조하는 공용 데이터라 ingest 밖에 둠). `findBySigun` 정도면 충분.~~
2. ~~**`Course` 엔티티 + `CourseRepository` 작성** — `course/Course.kt`, `course/CourseRepository.kt`
   (마찬가지로 `region/`과 같은 이유로 ingest 밖). FK는 `@ManyToOne`
   관계 대신 스칼라 `regionId: Long`, `rawDurunubiId: Long` 컬럼으로 단순하게 (프로젝트에 아직 엔티티 간 관계 매핑 선례가 없고, 지금 스코프에서 필요하지도 않음).
   `CourseRepository`에
   `existsByRawDurunubiId(rawDurunubiId: Long): Boolean` 추가 (멱등성 체크용).~~
3. **`CourseConversionService` 작성** (`ingest/course/CourseConversionService.kt`)
    - `RawDurunubiRepository.findAll()`로 전체 순회
    - 각 건: `raw_payload`에서 `sigun` 파싱 → `RegionRepository.findBySigun(sigun)`으로 region 조회 → 없으면 스킵 (카운트만 증가)
    - 있으면 `existsByRawDurunubiId` 체크 → 이미 있으면 스킵 → 없으면 `Course` 엔티티로 변환 후 저장
    - 필드 매핑: `crsKorNm`→name, `crsDstnc`(km 문자열)→`distance_m`(정수, m 단위로 변환: `*1000`
      반올림), `crsLevel`("1"/"2"/"3")→`difficulty`("쉬움"/"보통"/"어려움", 아래 미확정 사항 참고),
      `terrain_type`은 일단 null (아래 미확정 사항 참고), `gpxpath`→`gpx_storage_ref`(아래 미확정 사항 참고)
    - 반환값: 변환 건수 / 지역 없어 스킵 / 이미 존재해 스킵, 3가지 카운트
4. **`CourseConversionBatchRunner` 작성** (`ingest/course/CourseConversionBatchRunner.kt`) —
   `DurunubiCourseBatchRunner` 패턴 그대로 (`CommandLineRunner`, 로그 시작/종료). `@Profile`은 새 프로파일 (예: `batch-course-convert`)을
   쓸지 기존 `batch-durunubi`를 재사용할지 결정 필요 (아래 미확정 사항).
5. 필요하면 `application-batch-course-convert.yml` 추가 (`application-batch-durunubi.yml`과 동일하게
   `spring.main.web-application-type: none`만).
6. 로컬 실행: `./gradlew bootRun --args='--spring.profiles.active=<선택한 프로파일명>'` 로 돌리고 로그 + `select count(*) from courses` 로
   확인. 한 번 더 돌려서 count가 그대로인지 (멱등성) 확인.

---

## 파일별 작업 내역

| 파일 경로                                                   | 작업 내용                                               | 신규/수정 |
|-------------------------------------------------------------|---------------------------------------------------------|-----------|
| `course/Course.kt`                                          | `courses` 테이블 JPA 엔티티                             | 신규      |
| `course/CourseRepository.kt`                                | `JpaRepository<Course, Long>` + `existsByRawDurunubiId` | 신규      |
| `region/Region.kt`                                          | `regions` 테이블 JPA 엔티티                             | 신규      |
| `region/RegionRepository.kt`                                | `JpaRepository<Region, Long>` + `findBySigun`           | 신규      |
| `ingest/course/CourseConversionService.kt`                  | raw_durunubi → courses 변환 로직                        | 신규      |
| `ingest/course/CourseConversionBatchRunner.kt`              | `CommandLineRunner`, 배치 진입점                        | 신규      |
| `application-batch-course-convert.yml` (프로파일명 확정 후) | 배치 실행용 최소 설정                                   | 신규      |

---

## 참고할 기존 코드

- `ingest/durunubi/RawDurunubi.kt` — 엔티티 작성 패턴 (일반 `class`, `@Id` + `IDENTITY`, 생성자 프로퍼티 방식). `Course`/`Region` 엔티티 작성 시
  그대로 따라가면 됨.
- `ingest/durunubi/DurunubiCourseIngestService.kt` — `@Transactional` 서비스 패턴. 다만 여기 쓰인
  `replaceAll`(delSert: 전체삭제 후 재적재)은 **이번 티켓에는 안 맞음** — `courses`는
  `course_points`가 `ON DELETE CASCADE`로 걸려있어서, raw 테이블처럼 매번 싹 지우고 다시 넣으면 이미 쌓인 경로 데이터가 같이 날아감. 대신 "이미 있으면 스킵" 방식 (3번 참고)
  으로 가야 함.
- `ingest/durunubi/DurunubiCourseBatchRunner.kt` — `CommandLineRunner` + `@Profile` 배치 진입점 패턴. 로그 스타일까지 그대로 참고.
- `ingest/utils/IngestUtils.kt` — 이번 티켓에서 직접 쓸 유틸은 없어 보임 (JSON 파싱은
  `RawDurunubi.rawPayload`를 다시 `ObjectMapper`로 읽으면 됨).

---

## API 연동

해당 없음 — 이번 티켓은 내부 DB (`raw_durunubi` → `courses`) 간 변환이라 외부 API 호출 없음.

---

## 주의사항 / 흔히 하는 실수

- **delSert 패턴을 그대로 가져오면 안 됨** — 위 "참고할 기존 코드"에서 설명한 대로, `courses`는 하위 테이블 (`course_points`)이 딸려있어서 raw 테이블과 같은 방식으로 지우면
  안 된다.
- `crsDstnc`, `crsLevel` 등은 전부 raw_payload 안에서 **문자열**이다 — 파싱 실패 (빈 문자열, 예상 못한 값) 방어 없이 `.toInt()`/`.toDouble()` 그대로 쓰면
  배치 중간에 예외로 죽을 수 있음. 파싱 실패 시 해당 필드만 null 처리하고 계속 진행할지, 그 코스 자체를 스킵할지 정책을 정해야 함.
- `regions.sigun`은 원본 표기 그대로 ("경남 거제시")라 대소문자/공백 변형에 민감하지 않지만,
  `raw_payload->>'sigun'` 값이 예상과 다르게 앞뒤 공백이 있거나 하면 조인이 조용히 실패 (매칭 0건)할 수 있다 — 매칭 안 된 건수가 비정상적으로 많으면 (예: 144건 중 100건 이상
  스킵) 이 가능성부터 의심.
- FK 위반: `region_id`/`raw_durunubi_id`는 NOT NULL FK라, region 조회 실패한 건은 반드시 insert 시도 자체를 건너뛰어야 한다 (null로 넣으려 하면 컴파일
  단계에서든 DB 제약에서든 막힘).

---

## 테스트/검증 방법

- 로컬 Supabase (`supabase start` 상태)에서 배치 프로파일로 `bootRun` 실행, 콘솔 로그로 변환/스킵 건수 확인.
- `psql`로 `select count(*) from courses;` → 7건 (현재 regions 기준) 확인.
- `select c.name, c.distance_m, c.difficulty, r.name from courses c join regions r on r.id =
  c.region_id;` 로 몇 건 눈으로 대조 (원본 `crsKorNm`/`crsDstnc`/`crsLevel`과 비교).
- 배치를 한 번 더 실행해서 `count(*)`가 그대로 7건인지 확인 (멱등성 검증).
- 실기기/E2E 테스트 불필요 (서버 내부 배치).

---

## 미확정 사항 (아래 질문으로 다시 정리)

1. **`crsLevel` "1"/"2"/"3" → "쉬움"/"보통"/"어려움" 매핑이 맞는지** — Mock 코드가 3단계 텍스트를 쓰고 있고 실제 데이터도 정확히 3개 값 (1/2/3)만 나와서 정황상 맞아
   보이지만, 두루누비 공식 문서로 재확인은 안 했음. 이대로 가정하고 진행해도 될지?
2. **`terrain_type`을 이번 배치에서 채울지** — 신뢰할 만한 소스 필드가 raw_payload에 없음 (`brdDiv`는 144건 전부 고정값). null로 비워두고 넘어가도 되는지, 아니면
   `crsSummary`/
   `crsContents` 텍스트에서 "해안"/"강변"/"공원" 같은 키워드로 추정하는 로직까지 이번에 넣을지?
3. **`gpx_storage_ref`를 어떻게 채울지** — Supabase Storage 업로드 파이프라인이 아직 없음. (a) 일단 원본 `gpxpath` 외부 URL을 그대로 넣어두고 나중에 Storage
   마이그레이션 시 갈아끼울지, (b) null로 비워두고 이 컬럼은 완전히 별도 티켓으로 미룰지?
4. **멱등성 보장 방식** — `courses.raw_durunubi_id`에 DB unique 제약이 없음. (a) 애플리케이션에서
   `existsByRawDurunubiId` 체크만으로 갈지, (b) 마이그레이션을 하나 추가해서 `raw_durunubi_id`에 unique 제약을 걸어 DB 레벨에서도 이중 방어할지?
5. ~~**`Region` 엔티티 위치**~~ — **해결됨** (`ai/response.md` "패키지 설계 구조" 참고). `region/`,
   `course/` 최상위 패키지에 엔티티+레포지토리를 두고, `ingest/course/`에는 배치 (변환 서비스+러너)
   로직만 남긴다. 위 "구현 순서"/"파일별 작업 내역"에 반영 완료.
6. **배치 프로파일 이름** — 기존 `batch-durunubi` 프로파일을 재사용하면 두루누비/TourAPI 원본 재수집 배치까지 같이 실행돼 버림 (같은 프로파일에 묶인 다른
   `CommandLineRunner`들도 함께 뜸). 변환 배치만 따로 돌릴 수 있게 새 프로파일 (`batch-course-convert` 등)을 만드는 게 맞다고 보는데, 맞는지 확인 부탁.
