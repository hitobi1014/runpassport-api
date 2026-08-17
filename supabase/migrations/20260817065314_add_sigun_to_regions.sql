-- ============================================================================
-- regions: 원본 sigun 표기 컬럼 추가
-- (두루누비 raw_durunubi.raw_payload->>'sigun' 값("강원 강릉시" 등)과 분리 없이
--  1:1로 직접 매칭할 수 있도록 원문 그대로 담는 컬럼. sido/sigungu는 그대로 유지)
-- ============================================================================
alter table public.regions
    add column sigun text not null,
    drop column sido,
    drop column sigungu;

comment on column public.regions.sigun is '원본 시군 표기 (두루누비 sigun 필드, 예: 강원 강릉시)';


insert into public.regions (name, sigun, center_lat, center_lng)
values ('서울', '서울 강동구', 37.5301, 127.1238),
       ('일산', '경기 고양시', 37.6584, 126.7710),
       ('거제', '경남 거제시', 34.8806, 128.6212);