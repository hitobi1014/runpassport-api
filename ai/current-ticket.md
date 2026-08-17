# [BE] regions 마스터 데이터 시딩

> 상태: IN_PROGRESS
> 시작일: 2026-08-17

---

## 목표

`regions`(시도/시군구 단위 지역 마스터) 테이블에 데이터를 채운다. `courses.region_id`가
`not null` FK로 `regions`를 참조하기 때문에, 다음 단계인 "raw_durunubi → courses 변환 배치"를 시작하려면 이게 먼저 끝나 있어야 한다.

```sql
create table public.regions
(
    id         bigint generated always as identity primary key,
    sido       text          not null,
    sigungu    text          not null,
    name       text          not null,
    center_lat numeric(9, 6) not null,
    center_lng numeric(9, 6) not null,
    constraint regions_sido_sigungu_key unique (sido, sigungu)
);
```

---

## 실제 데이터로 확인한 것 — 지금 당장 필요한 지역은 57개뿐

전국 시군구를 다 채우려면 ~250개나 되는데, `raw_durunubi`에 실제로 수집된 코스가 다루는 지역만 먼저 보면 범위가 훨씬 작다:

```sql
select distinct raw_payload ->> 'sigun'
from public.raw_durunubi
order by 1;
-- 57개 행: "강원 강릉시", "강원 고성군", ..., "충남 태안군" 등
```

시도별 분포:

```
강원 35 · 전남 29 · 경북 17 · 충남 17 · 경기 15 · 경남 14 · 전북 6 · 울산 4 · 부산 4 · 인천 2 · 서울 1
```

즉 **11개 시도, 57개 시군구**면 지금 확보된 코스 데이터를 courses로 옮기는 데는 충분하다. 전국 250개를 미리 다 채워두는 것보다, 지금 필요한 57개만 먼저 채우고 나중에 코스가 늘어나면 그때
같이 늘리는 쪽이 낫다고 본다 (안 쓰이는 데이터를 미리 만들 이유가 없음) — 이견 있으면 알려줄 것.

### `sido`/`sigungu` 컬럼값은 `sigun` 원본 표기를 그대로 쓰는 걸 제안

`raw_payload->>'sigun'`이 이미 `"강원 강릉시"`처럼 "시도 시군구" 공백 구분 형태로 되어 있다.
`regions.sido`/`sigungu`를 이 표기 그대로 (예: `sido="강원"`, `sigungu="강릉시"`) 저장해두면, 나중에 courses 변환 배치에서
`sigun.split(" ", limit=2)`로 바로 `regions`를 조회할 수 있어서 매핑 테이블이 따로 필요 없다. (참고: 강원도는 2023년에 강원특별자치도로 공식 개칭됐지만 두루누비 데이터는 여전히
옛 표기 "강원"을 쓰고 있음 — 원본 API 표기에 맞추는 게 나중에 조인할 때 안 헷갈린다.)

**[완료]** `sido`/`sigungu`로 split해서 저장하는 것과 별개로, 원본 `sigun` 문자열 ("강원 강릉시")을 분리 없이 그대로 담는 `regions.sigun text not null`
컬럼을 추가했다 —
`raw_durunubi.raw_payload->>'sigun'` 값과 1:1로 바로 매칭할 수 있게 하기 위함 (courses 변환 배치에서 split 없이 바로 조인 가능). `sido`/`sigungu`는
구조화된 조회용으로 그대로 유지. 마이그레이션: `supabase/migrations/20260817065314_add_sigun_to_regions.sql`, 로컬 적용/검증 완료.

---

## 확인 필요 — 위경도 (중심좌표) 소스를 어떻게 할지

**이 부분은 직접 결정할 것.** `center_lat`/`center_lng`는 57개 시군구 전부에 필요한데, 내가 기억만으로 채우면 특히 잘 안 알려진 군 단위는 오차가 클 수 있고, 검증할 방법이 마땅치
않다 (과거에 두루누비 API 스펙을 문서만 믿고 가정했다가 실제 응답이랑 달랐던 적이 있었던 것과 같은 종류의 위험 — 이번엔 좌표라 틀려도 눈에 안 띄고 조용히 넘어갈 수 있어서 더 조심스럽다).

선택지:

1. **공공데이터 기준 좌표 사용** — 행정안전부/통계청이 배포하는 "행정구역 중심좌표" 류 공개 데이터셋을 받아서 57개만 추려 매핑. 가장 신뢰도 높음, 다운로드/추출 작업 필요.
2. **지오코딩 API로 즉석 조회** — "강원도 강릉시청" 같은 주소를 카카오/구글 지오코딩에 넣어서 좌표를 받아옴. 정확도는 준수하지만, 이것도 결국 새 외부 API 연동이 하나 더 생기는 것 — 57개
   정도면 배보다 배꼽이 클 수 있음.
3. **AI가 대략적인 좌표를 채우고, 데모 목적상 오차 허용** — 시/군청 소재지 기준으로 대략적인 값을 채울 수는 있는데, **정확도를 보장 못 한다**는 걸 분명히 하고 싶다. 특히
   `numeric(9,6)`
   스키마가 소수점 6자리 (서브미터 단위)까지 기대하는 것처럼 보이는데, 이 프로젝트에서
   `center_lat/lng`가 실제로 어디 쓰이는지 (지역별 날씨 조회 좌표 정도로 보임 — GPS 코스 인증 자체는 `course_points`의 별도 좌표를 씀)에 따라 "시청 근처면 충분"할 수도 있다.

어느 쪽으로 갈지, 혹은 정확도 요구수준이 어느 정도인지 (날씨 API 좌표용이라 대략적이어도 되는지)
알려주면 그에 맞게 시드 데이터를 준비하겠다.

---

## 작업 순서 (방향 정해지면 진행)

1. 위 좌표 소스 결정
2. 마이그레이션이 아니라 **시드 데이터**이므로, Supabase 마이그레이션 파일에 `insert` 문으로 넣을지, 아니면 앱 기동 시 실행되는 별도 시딩 배치 (두루누비 패턴처럼
   `CommandLineRunner`)로 넣을지 결정 — 57건 고정 데이터라 마이그레이션 `insert`가 더 단순해 보이지만, 이견 있으면 논의
    - 두루누비 패턴으로 진행 => 스크립트로 실행
3. 57개 `(sido, sigungu, sigun, name, center_lat, center_lng)` 데이터 준비
    - raw_durunubi와 동일한 컬럼으로 매핑 sigun
4. 마이그레이션 적용 후 `regions` 테이블에 57건 들어간 것 확인

---

## 완료 조건 체크리스트

- [x] `regions.sigun` 컬럼 추가 (raw_durunubi.sigun과 직접 매칭용)
- [ ] 좌표 소스 결정
- [ ] `regions`에 57개 시군구 데이터 적재 (마이그레이션 or 시딩 배치)
- [ ] 적재 확인 (`select count(*) from regions` 등으로 눈에 보이는 근거 남기기)

---

# GPX 참고

# GPX 파일 → regions 데이터 매핑 방법 (기술 설계)

좌표 값 자체 (어떤 지역이 몇 도 몇 분인지, DMZ 오표기 건 등)는 이 문서에서 다루지 않는다.
**"GPX 파일에서 어떻게 좌표 데이터를 뽑아서 DB까지 매핑할 것인가"**라는 파이프라인 설계만 정리.

## 전체 흐름

```
raw_durunubi (이미 적재됨)
  └─ raw_payload ->> 'gpxpath'  (코스 1건당 GPX 파일 URL 1개)
  └─ raw_payload ->> 'sigun'    (코스가 속한 지역, 예: "강원 강릉시")
        │
        │ 1. HTTP GET
        ▼
   GPX 파일 (XML, <trkpt lat=".." lon="..">  다수)
        │
        │ 2. XML 파싱 → trkpt lat/lon 좌표 리스트 추출
        ▼
   코스 1건의 좌표 목록 [(lat, lon), (lat, lon), ...]
        │
        │ 3. 코스 단위 집계 (평균)
        ▼
   코스 1건의 중심좌표 (lat, lon)
        │
        │ 4. sigun 기준으로 그룹핑 → 그룹 내 코스 중심좌표들을 다시 평균
        ▼
   지역(sigun) 단위 중심좌표
        │
        │ 5. regions.sigun 매칭 → center_lat/center_lng 갱신(UPSERT)
        ▼
   regions 테이블
```

`regions.sigun` 컬럼 (이번에 추가함)이 이 파이프라인의 핵심 연결고리다 — `raw_durunubi.sigun`과
`regions.sigun`이 원문 그대로 1:1 매칭되므로, 중간에 별도 매핑 테이블 없이 바로 group-by/join이 가능하다.

## 단계별 기술 디테일

### 1. GPX 파일 가져오기

- 소스는 `raw_durunubi.raw_payload ->> 'gpxpath'` — 이미 DB에 있으므로 별도 API 호출 없이 SQL 한 번으로 144개 URL 전부 뽑을 수 있음.
- 각 URL은 순수 정적 파일 (GPX 1.1 XML)이라 인증/서비스키 불필요. `RestClient`(이미 전역
  `LoggingRestClientCustomizer` 적용돼 있음)로 그대로 GET하면 됨 — 새 RestClient 설정 필요 없음.

### 2. GPX 파싱 — 정식 XML 파서 사용 권장

검증 단계에서는 빠르게 확인하려고 정규식 (`trkpt lat="([\-0-9.]+)" lon="([\-0-9.]+)"`)으로 뽑았지만, **이건 검증용이었고 실제 구현에는 권장하지 않는다.** 정규식은 속성 순서가
바뀌거나 (`lon`이 `lat`보다 먼저 오는 등), 줄바꿈/네임스페이스가 섞이면 조용히 깨질 수 있다. 코틀린 구현에서는:

- JVM 표준 `javax.xml.parsers.DocumentBuilder`(추가 의존성 없음)로 `gpx > trk > trkseg > trkpt`
  경로를 순회하며 `lat`/`lon` 속성을 읽거나
- 이미 프로젝트에 있는 Jackson (`tools.jackson`)의 XML 모듈을 추가해서 파싱하거나
- 가벼운 GPX 전용 파싱 라이브러리를 쓰는 것

중 하나를 쓰는 게 안전하다. GPX 스펙 자체가 단순해서 (`<trkpt lat lon><ele/></trkpt>` 반복 구조)
DOM 파서로도 코드량이 많지 않다.

### 3. 코스 단위 집계

코스 하나의 GPX 안에 있는 모든 `trkpt`의 `lat`/`lon`을 산술평균 → 그 코스의 대표 좌표 1개. (코스가 길든 짧든, trackpoint 밀도가 균일하다는 전제하에 "코스가 지나는 경로의 대략적인
무게중심"
정도로 보면 된다 — GPS 트랙 특성상 도로가 굽는 구간에 포인트가 몰릴 수 있어 완벽한 기하학적 중심은 아니지만, region 좌표 용도로는 충분한 근사치.)

### 4. 지역 단위 집계

`sigun` 값으로 그룹핑한 뒤, 그 그룹에 속한 코스들의 (3번에서 구한) 중심좌표를 다시 평균. 코스가 여러 개인 지역일수록 평균이 더 안정적이고, 코스가 1개뿐인 지역은 사실상 그 코스의 좌표를 그대로 쓰는
셈이 된다 (이 부분의 정확도 트레이드오프는 별도로 이미 공유한 내용이라 여기선 생략).

### 5. `regions` 반영

`sigun` 값을 키로 `regions.center_lat`/`center_lng`를 채운다. 이 작업 자체는 "매 배치마다 반복 실행"할 필요가 없는 **일회성 시드 작업**에 가깝다 — 코스가 나중에 더
늘어나면 그때 다시 계산해서 갱신하는 정도면 충분.

## 구현 위치는 두 가지 방식 중 선택 가능

- **A. 일회성 스크립트로 계산 → 결과값을 마이그레이션 `insert`/`update` 문에 박아넣기**
  (지금까지 한 사전 조사와 같은 방식). 코드베이스에 영구적으로 남는 로직이 아니라, "값을 구하는 한 번의 작업"으로 취급. 구현/유지보수 부담이 가장 적음.
- **B. 코틀린 컴포넌트로 구현** (`ingest` 패키지 스타일 재사용) — GPX를 가져와 파싱하고 지역별로 집계해서 `regions`에 반영하는 로직을 코드로 남겨두는 방식. 코스가 계속 늘어나는 걸
  감안해서
  "언제든 재계산 가능한 배치"로 만들고 싶다면 이쪽이 맞음. 두루누비/TourAPI Fetcher 패턴 (`RestClient` 주입, `normalizeToNodeList`류 유틸 재사용)을 그대로 가져다 쓸
  수 있음 — 다만 이건 JSON이 아니라 XML이라 파싱 유틸은 새로 만들어야 함.

어느 쪽으로 갈지는 `current-ticket.md`의 "마이그레이션 insert vs 시딩 배치" 결정과 같이 묶어서 판단하면 될 것 같다.
