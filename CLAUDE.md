# CLAUDE.md

## 프로젝트 개요

Run패스포트 — 관광공사 공모 출품용 러닝 코스 추천 + GPS 궤적 기반 완주 인증 + 지자체 스탬프/쿠폰 리워드 서비스의 백엔드 API 서버. Android 앱과 공무원용 웹 대시보드가 이 서버 하나만
바라본다. 서버 로직은 이 리포 하나로 통합되어 있고 별도 Node 서버나 Supabase Edge Function은 쓰지 않는다.

## 기술 스택

- Kotlin + Spring Boot 4.1.0, JDK 21 (Eclipse Temurin), Gradle-Kotlin
- Web: spring-boot-starter-web / WebClient (webflux, 외부 공공 API 호출 전용)
- DB: Spring Data JPA + org.postgresql:postgresql → Supabase (Managed PostgreSQL)
- 인증: spring-boot-starter-security + jjwt (카카오 로그인 토큰 검증 후 자체 JWT 발급/검증. **Supabase Auth 미사용**)
- 문서화: springdoc-openapi (Swagger UI — 공개 repo라 필수)
- 스케줄링: `@Scheduled` (공공 API 배치 수집)
- 전체 의존성 버전: 개발 환경 노션 페이지 참고

## 핵심 아키텍처 규칙 (반드시 지킬 것)

1. **Supabase는 DB 전용.** Auth/Edge Function/Storage는 쓰지 않는다. 서버 로직은 전부 이 Spring Boot 앱 안에서 처리한다.
2. **공공 API (TourAPI/기상청/에어코리아/두루누비)는 클라이언트가 직접 호출하지 않는다.** 반드시 이 서버가 배치로 수집 → `raw_*` 테이블에 원본 jsonb 저장 → 가공 후 `public`
   스키마 테이블로 적재. 런타임 조회는 항상 우리 DB만 본다.
3. **완주 판정은 서버 권위.** 앱이 "완주함"이라고 보낸 값을 그대로 믿지 않는다 — 궤적 원본을 받아 커버리지 비율 (≥85%) + 진행방향 + 페이스를 서버에서 다시 계산해 최종 판정한다.
4. **쿠폰 발급/사용은 반드시 서버 트랜잭션으로 처리.** 매장 PIN 4자리 검증 후 상태를 `used`로 변경하며 되돌릴 수 없다. 중복 사용 방지가 핵심.
5. **DB 접속은 Session Pooler (5432)를 쓴다.** Transaction Pooler (6543)는 JPA prepared statement와 궁합이 나쁘다. `dev` 프로필의
   `ddl-auto`는 반드시 `validate` 또는 `none`으로 고정한다.
6. 비밀번호/API 키는 `.env` / `application-*.yml`에만 두고 커밋하지 않는다.

## 자주 쓰는 명령어

- ./gradlew bootRun # 로컬 서버 기동
- ./gradlew test # 테스트
- ./gradlew build # 빌드

## 폴더 구조

TBD — 프로젝트 시작 시 정의

## 코딩 규칙 / 커밋 컨벤션

TBD — 팀 협의 후 정의

## 더 알아야 할 것

기획/기능 상세는 기능 명세, 의존성/프로젝트 메타는 개발 환경, 스키마는 DB 스키마, Supabase 운영 관련은 supabase 노션 페이지를 참고한다.