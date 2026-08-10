-- ============================================================================
-- run-passport initial schema
-- Generated from ERD (regions / raw ingestion / courses / run tracking / rewards)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- regions: 시도/시군구 단위 지역 마스터
-- ----------------------------------------------------------------------------
create table public.regions (
    id          bigint generated always as identity primary key,
    sido        text        not null,
    sigungu     text        not null,
    name        text        not null,
    center_lat  numeric(9, 6) not null,
    center_lng  numeric(9, 6) not null,
    constraint regions_sido_sigungu_key unique (sido, sigungu)
);

comment on table public.regions is '시도/시군구 단위 지역 마스터';
comment on column public.regions.sido is '시도';
comment on column public.regions.sigungu is '시군구';
comment on column public.regions.name is '지역명(표시용)';
comment on column public.regions.center_lat is '중심 위도';
comment on column public.regions.center_lng is '중심 경도';

-- ----------------------------------------------------------------------------
-- raw_durunubi: 두루누비 원본 응답 적재
-- ----------------------------------------------------------------------------
create table public.raw_durunubi (
    id            bigint generated always as identity primary key,
    external_id   text        not null,
    raw_payload   jsonb       not null,
    collected_at  timestamptz not null default now(),
    constraint raw_durunubi_external_id_key unique (external_id)
);

comment on table public.raw_durunubi is '두루누비 API 원본 응답 적재';
comment on column public.raw_durunubi.external_id is '코스ID';
comment on column public.raw_durunubi.raw_payload is '원본 응답 JSON';
comment on column public.raw_durunubi.collected_at is '수집시간';

-- ----------------------------------------------------------------------------
-- raw_tour: TourAPI 원본 응답 적재
-- ----------------------------------------------------------------------------
create table public.raw_tour (
    id               bigint generated always as identity primary key,
    external_id      text        not null,
    content_type_id  integer     not null,
    raw_payload      jsonb       not null,
    collected_at     timestamptz not null default now(),
    constraint raw_tour_external_id_content_type_id_key unique (external_id, content_type_id)
);

comment on table public.raw_tour is 'TourAPI 원본 응답 적재';
comment on column public.raw_tour.external_id is '코스ID';
comment on column public.raw_tour.content_type_id is '관광타입(39 음식점/38 쇼핑 등)';
comment on column public.raw_tour.raw_payload is '원본 응답 JSON';
comment on column public.raw_tour.collected_at is '수집시간';

-- ----------------------------------------------------------------------------
-- courses: 러닝 코스
-- ----------------------------------------------------------------------------
create table public.courses (
    id               bigint generated always as identity primary key,
    raw_durunubi_id  bigint      not null references public.raw_durunubi (id) on delete restrict,
    region_id        bigint      not null references public.regions (id) on delete restrict,
    name             text        not null,
    distance_m       integer,
    difficulty       text,
    terrain_type     text,
    gpx_storage_ref  text,
    created_at       timestamptz not null default now()
);

comment on table public.courses is '러닝 코스';
comment on column public.courses.raw_durunubi_id is '원천 raw row';
comment on column public.courses.region_id is '지역';
comment on column public.courses.name is '코스명';
comment on column public.courses.distance_m is '총 거리(m)';
comment on column public.courses.difficulty is '난이도';
comment on column public.courses.terrain_type is '지형(강변/해안/공원 등)';
comment on column public.courses.gpx_storage_ref is 'GPX 파일 참조(Storage)';
comment on column public.courses.created_at is '적재일시';

create index courses_region_id_idx on public.courses (region_id);
create index courses_raw_durunubi_id_idx on public.courses (raw_durunubi_id);

-- ----------------------------------------------------------------------------
-- course_points: 코스 경로 포인트
-- ----------------------------------------------------------------------------
create table public.course_points (
    id         bigint generated always as identity primary key,
    course_id  bigint      not null references public.courses (id) on delete cascade,
    seq        integer     not null,
    lat        numeric(9, 6) not null,
    lng        numeric(9, 6) not null,
    constraint course_points_course_id_seq_key unique (course_id, seq)
);

comment on table public.course_points is '코스 경로 포인트';
comment on column public.course_points.course_id is '코스';
comment on column public.course_points.seq is '순번';
comment on column public.course_points.lat is '위도';
comment on column public.course_points.lng is '경도';

-- ----------------------------------------------------------------------------
-- pois: 관심지점 (TourAPI 기반)
-- ----------------------------------------------------------------------------
create table public.pois (
    id           bigint generated always as identity primary key,
    raw_tour_id  bigint      not null references public.raw_tour (id) on delete restrict,
    course_id    bigint      references public.courses (id) on delete set null,
    name         text        not null,
    category     text,
    lat          numeric(9, 6) not null,
    lng          numeric(9, 6) not null
);

comment on table public.pois is '관심지점(POI)';
comment on column public.pois.raw_tour_id is '원천 raw row';
comment on column public.pois.course_id is '인접 코스';
comment on column public.pois.name is '이름';
comment on column public.pois.category is '분류';
comment on column public.pois.lat is '위도';
comment on column public.pois.lng is '경도';

create index pois_raw_tour_id_idx on public.pois (raw_tour_id);
create index pois_course_id_idx on public.pois (course_id);

-- ----------------------------------------------------------------------------
-- merchants: 제휴 매장 (TourAPI 시딩)
-- ----------------------------------------------------------------------------
create table public.merchants (
    id           bigint generated always as identity primary key,
    raw_tour_id  bigint      not null references public.raw_tour (id) on delete restrict,
    course_id    bigint      references public.courses (id) on delete set null,
    region_id    bigint      not null references public.regions (id) on delete restrict,
    name         text        not null,
    category     text,
    label        text
);

comment on table public.merchants is '제휴 매장(리워드 사용처)';
comment on column public.merchants.raw_tour_id is '원천 raw row(TourAPI 시딩)';
comment on column public.merchants.course_id is '인접 코스';
comment on column public.merchants.region_id is '지역(리워드 귀속)';
comment on column public.merchants.name is '매장명';
comment on column public.merchants.category is '음식점/쇼핑';
comment on column public.merchants.label is '제휴 매장(예시) 라벨';

create index merchants_raw_tour_id_idx on public.merchants (raw_tour_id);
create index merchants_course_id_idx on public.merchants (course_id);
create index merchants_region_id_idx on public.merchants (region_id);

-- ----------------------------------------------------------------------------
-- region_weather_current: 지역별 최신 날씨
-- ----------------------------------------------------------------------------
create table public.region_weather_current (
    id           bigint generated always as identity primary key,
    region_id    bigint      not null references public.regions (id) on delete cascade,
    fetched_at   timestamptz not null default now(),
    temp         numeric(5, 2),
    precip_prob  numeric(5, 2) check (precip_prob between 0 and 100),
    wind_speed   numeric(5, 2),
    pm10         numeric(6, 2),
    pm25         numeric(6, 2),
    constraint region_weather_current_region_id_key unique (region_id)
);

comment on table public.region_weather_current is '지역별 최신 날씨(지역당 1행, 매 수집시 갱신)';
comment on column public.region_weather_current.region_id is '지역';
comment on column public.region_weather_current.fetched_at is '수집시간';
comment on column public.region_weather_current.temp is '체감온도';
comment on column public.region_weather_current.precip_prob is '강수확률';
comment on column public.region_weather_current.wind_speed is '풍속';
comment on column public.region_weather_current.pm10 is '미세먼지';
comment on column public.region_weather_current.pm25 is '초미세먼지';

-- ----------------------------------------------------------------------------
-- running_index: 지역별 러닝지수 (이력)
-- ----------------------------------------------------------------------------
create table public.running_index (
    id             bigint generated always as identity primary key,
    region_id      bigint      not null references public.regions (id) on delete cascade,
    calculated_at  timestamptz not null default now(),
    score          numeric(5, 2) not null check (score between 0 and 100)
);

comment on table public.running_index is '지역별 러닝지수 계산 이력';
comment on column public.running_index.region_id is '지역';
comment on column public.running_index.calculated_at is '계산시간';
comment on column public.running_index.score is '러닝지수(0~100)';

create index running_index_region_id_calculated_at_idx
    on public.running_index (region_id, calculated_at desc);

-- ----------------------------------------------------------------------------
-- users: 사용자
-- ----------------------------------------------------------------------------
create table public.users (
    id          bigint generated always as identity primary key,
    kakao_id    text        not null,
    nickname    text,
    created_at  timestamptz not null default now(),
    constraint users_kakao_id_key unique (kakao_id)
);

comment on table public.users is '사용자';
comment on column public.users.kakao_id is '카카오 고유ID';
comment on column public.users.nickname is '닉네임';
comment on column public.users.created_at is '가입일시';

-- ----------------------------------------------------------------------------
-- run_sessions: 러닝 세션
-- ----------------------------------------------------------------------------
create table public.run_sessions (
    id              bigint generated always as identity primary key,
    user_id         bigint      not null references public.users (id) on delete cascade,
    course_id       bigint      not null references public.courses (id) on delete restrict,
    started_at      timestamptz not null,
    ended_at        timestamptz,
    distance_m      integer,
    duration_s      integer,
    avg_pace        numeric(6, 2),
    coverage_ratio  numeric(5, 2) check (coverage_ratio between 0 and 100),
    verified        boolean     not null default false,
    reject_reason   text
);

comment on table public.run_sessions is '러닝 세션';
comment on column public.run_sessions.user_id is '사용자';
comment on column public.run_sessions.course_id is '코스';
comment on column public.run_sessions.started_at is '시작시간';
comment on column public.run_sessions.ended_at is '종료시간';
comment on column public.run_sessions.distance_m is '거리(m)';
comment on column public.run_sessions.duration_s is '소요시간(초)';
comment on column public.run_sessions.avg_pace is '평균 페이스';
comment on column public.run_sessions.coverage_ratio is '코스 커버리지 비율';
comment on column public.run_sessions.verified is '서버 완주 인증 여부';
comment on column public.run_sessions.reject_reason is '반려 사유';

create index run_sessions_user_id_idx on public.run_sessions (user_id);
create index run_sessions_course_id_idx on public.run_sessions (course_id);

-- ----------------------------------------------------------------------------
-- run_tracks: 런 세션 궤적 (1:1)
-- ----------------------------------------------------------------------------
create table public.run_tracks (
    id          bigint generated always as identity primary key,
    session_id  bigint not null references public.run_sessions (id) on delete cascade,
    points      jsonb,
    constraint run_tracks_session_id_key unique (session_id)
);

comment on table public.run_tracks is '런 세션 궤적';
comment on column public.run_tracks.session_id is '런 세션(1:1)';
comment on column public.run_tracks.points is '궤적 포인트(JSONB) 또는 Storage 참조';

-- ----------------------------------------------------------------------------
-- stamps: 지역별 스탬프
-- ----------------------------------------------------------------------------
create table public.stamps (
    id          bigint generated always as identity primary key,
    user_id     bigint      not null references public.users (id) on delete cascade,
    region_id   bigint      not null references public.regions (id) on delete cascade,
    count       integer     not null default 0,
    updated_at  timestamptz not null default now(),
    constraint stamps_user_id_region_id_key unique (user_id, region_id)
);

comment on table public.stamps is '사용자별 지역 스탬프';
comment on column public.stamps.user_id is '사용자';
comment on column public.stamps.region_id is '지역(귀속)';
comment on column public.stamps.count is '스탬프 개수';
comment on column public.stamps.updated_at is '갱신일시';

-- ----------------------------------------------------------------------------
-- coupons: 리워드 쿠폰
-- ----------------------------------------------------------------------------
create table public.coupons (
    id           bigint generated always as identity primary key,
    user_id      bigint      not null references public.users (id) on delete cascade,
    region_id    bigint      not null references public.regions (id) on delete restrict,
    merchant_id  bigint      not null references public.merchants (id) on delete restrict,
    code         text        not null,
    status       text        not null default 'issued' check (status in ('issued', 'used')),
    issued_at    timestamptz not null default now(),
    expires_at   timestamptz,
    used_at      timestamptz,
    constraint coupons_code_key unique (code)
);

comment on table public.coupons is '리워드 쿠폰';
comment on column public.coupons.user_id is '사용자';
comment on column public.coupons.region_id is '지역';
comment on column public.coupons.merchant_id is '사용처 매장';
comment on column public.coupons.code is '유니크 코드';
comment on column public.coupons.status is 'issued/used';
comment on column public.coupons.issued_at is '발급일시';
comment on column public.coupons.expires_at is '유효기간';
comment on column public.coupons.used_at is '사용일시';

create index coupons_user_id_idx on public.coupons (user_id);
create index coupons_region_id_idx on public.coupons (region_id);
create index coupons_merchant_id_idx on public.coupons (merchant_id);

-- ----------------------------------------------------------------------------
-- Row Level Security
-- API를 통한 anon/authenticated 접근을 기본 차단. 앱 서버는 service_role/DB
-- 슈퍼유저로 직접 연결하므로 RLS의 영향을 받지 않음 (bypassrls).
-- ----------------------------------------------------------------------------
alter table public.regions enable row level security;
alter table public.raw_durunubi enable row level security;
alter table public.raw_tour enable row level security;
alter table public.courses enable row level security;
alter table public.course_points enable row level security;
alter table public.pois enable row level security;
alter table public.merchants enable row level security;
alter table public.region_weather_current enable row level security;
alter table public.running_index enable row level security;
alter table public.users enable row level security;
alter table public.run_sessions enable row level security;
alter table public.run_tracks enable row level security;
alter table public.stamps enable row level security;
alter table public.coupons enable row level security;
