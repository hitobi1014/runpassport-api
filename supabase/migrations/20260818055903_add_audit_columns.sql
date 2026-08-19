-- ============================================================================
-- BaseEntity(created_at/modified_at) 감사 컬럼 추가
-- 대상: raw_durunubi/raw_tour(배치 원본 적재 테이블)를 제외한 나머지 전체 테이블
-- courses/users는 created_at이 이미 있어서 modified_at만 추가.
-- stamps.updated_at, region_weather_current.fetched_at, running_index.calculated_at,
-- coupons.issued_at은 도메인 의미가 있는 별개 컬럼이라 그대로 두고 건드리지 않음.
-- ============================================================================

alter table public.regions
    add column created_at  timestamptz not null default now(),
    add column modified_at timestamptz not null default now();

alter table public.courses
    add column modified_at timestamptz not null default now();

alter table public.course_points
    add column created_at  timestamptz not null default now(),
    add column modified_at timestamptz not null default now();

alter table public.pois
    add column created_at  timestamptz not null default now(),
    add column modified_at timestamptz not null default now();

alter table public.merchants
    add column created_at  timestamptz not null default now(),
    add column modified_at timestamptz not null default now();

alter table public.region_weather_current
    add column created_at  timestamptz not null default now(),
    add column modified_at timestamptz not null default now();

alter table public.running_index
    add column created_at  timestamptz not null default now(),
    add column modified_at timestamptz not null default now();

alter table public.users
    add column modified_at timestamptz not null default now();

alter table public.run_sessions
    add column created_at  timestamptz not null default now(),
    add column modified_at timestamptz not null default now();

alter table public.run_tracks
    add column created_at  timestamptz not null default now(),
    add column modified_at timestamptz not null default now();

alter table public.stamps
    add column created_at  timestamptz not null default now(),
    add column modified_at timestamptz not null default now();

alter table public.coupons
    add column created_at  timestamptz not null default now(),
    add column modified_at timestamptz not null default now();

comment on column public.regions.created_at is '생성일시';
comment on column public.regions.modified_at is '수정일시';
comment on column public.courses.modified_at is '수정일시';
comment on column public.course_points.created_at is '생성일시';
comment on column public.course_points.modified_at is '수정일시';
comment on column public.pois.created_at is '생성일시';
comment on column public.pois.modified_at is '수정일시';
comment on column public.merchants.created_at is '생성일시';
comment on column public.merchants.modified_at is '수정일시';
comment on column public.region_weather_current.created_at is '생성일시';
comment on column public.region_weather_current.modified_at is '수정일시';
comment on column public.running_index.created_at is '생성일시';
comment on column public.running_index.modified_at is '수정일시';
comment on column public.users.modified_at is '수정일시';
comment on column public.run_sessions.created_at is '생성일시';
comment on column public.run_sessions.modified_at is '수정일시';
comment on column public.run_tracks.created_at is '생성일시';
comment on column public.run_tracks.modified_at is '수정일시';
comment on column public.stamps.created_at is '생성일시';
comment on column public.stamps.modified_at is '수정일시';
comment on column public.coupons.created_at is '생성일시';
comment on column public.coupons.modified_at is '수정일시';
