-- ============================================================================
-- raw_tour: 호출 엔드포인트/파라미터 컬럼 추가
-- ============================================================================
alter table public.raw_tour
    add column endpoint text  not null,
    add column params   jsonb not null;

comment on column public.raw_tour.endpoint is '호출한 TourAPI 오퍼레이션명 (예: locationBasedList2)';
comment on column public.raw_tour.params is '호출 파라미터';
