# 코드 리뷰: [BE] 관광공사 TourAPI 원본 데이터 배치 스크립트 작성

- 리뷰 일시: 2026-08-16
- 대상 변경: 워킹트리 변경사항
- 변경 파일:
  - `src/main/kotlin/.../ingest/tour/RealTourContentFetcher.kt`
  - `src/main/kotlin/.../ingest/utils/IngestUtils.kt`
  - `src/main/kotlin/.../ingest/durunubi/RealDurunubiCourseFetcher.kt`

---

## 완료 조건 대조

- [x] 두루누비와 같은 Fetcher 인터페이스 패턴으로 구현 — **충족**. `TourContentFetcher` 인터페이스 구현, Mock/Real 분리 패턴 적용됨
- [x] 실제 API 연동 — **충족**. `RealTourContentFetcher`에서 실제 API 호출 로직 구현됨

---

## 발견한 이슈

### 🔴 반드시 확인 (버그/로직 오류)

1. **RealTourContentFetcher.kt:31-41 - searchParam이 루프 내부에서 매번 재생성됨**

   `while (true)` 루프 안에서 `searchParam`을 매번 새로 생성하고 있는데, 페이지 번호만 바뀌고 나머지 파라미터는 동일하므로 루프 바깥으로 빼는 게 맞음. 성능 문제보다 **의도가 불명확해 보이는 게 문제** — 페이지마다 좌표가 바뀌는 것처럼 오해할 수 있음.

   ```kotlin
   // 루프 바깥으로 이동
   val searchParam = LocationBasedListSearchParam(...)
   val endpoint = "/locationBasedList2"

   while (true) {
       val responseBody = fetchPage(endpoint, pageNo, searchParam).path("response")
       ...
   }
   ```

2. **RealTourContentFetcher.kt:84 - params에 serviceKey가 그대로 직렬화됨**

   `objectMapper.writeValueAsString(searchParam)`을 하면 `serviceKey`가 DB에 평문으로 저장됨. 보안상 민감 정보가 `raw_tour.params` 컬럼에 노출됨.

   **해결 방안**:
   - `@JsonIgnore`로 serviceKey 필드 제외
   - 또는 params용 별도 DTO 사용 (serviceKey 없는 버전)

---

### 🟡 개선 제안 (컨벤션/가독성/코틀린다움)

1. **RealTourContentFetcher.kt:34 - contentTypeId 하드코딩**

   현재 `contentTypeId = 39` (음식점)만 수집함. current-ticket.md에서 "여러 content_type_id를 순회하면서 각각 호출해야 할 가능성" 언급했는데, 나중에 확장 예정이면 TODO 주석을 남기거나, 파라미터로 받도록 미리 설계 고려.

2. **RealTourContentFetcher.kt:66 - 불필요한 세미콜론**

   ```kotlin
   return items;  // 코틀린은 세미콜론 불필요
   ```

3. **LocationBasedListSearchParam 위치**

   `RealTourContentFetcher.kt` 파일 하단에 data class가 있는데, 별도 파일로 분리하거나 `TourContentFetcher.kt`와 같은 곳에 두는 게 나을 수 있음. 두루누비에서 DTO 위치 컨벤션이 어떻게 되어있는지 확인 후 맞추면 좋겠음.

4. **IngestUtils.kt - normalizeToNodeList 공통화 잘함**

   두루누비와 TourAPI에서 동일 패턴을 쓰므로 유틸로 빼놓은 건 좋은 리팩토링.

---

### 🟢 잘한 점

- `normalizeToNodeList`를 공통 유틸로 추출하여 두루누비/TourAPI에서 재사용
- 두루누비 Fetcher에서 중복됐던 `MobileApp` 파라미터 제거 (79번 줄 diff)
- 응답 파싱 구조가 두루누비 패턴을 잘 따름 (header 체크, body.items.item 추출)
- 페이지네이션 종료 조건 잘 처리함 (`pageItems.isEmpty()`, `items.size >= totalCount`, `pageItems.size < numOfRows`)

---

## 다음 액션

1. **필수**: `searchParam` 생성을 루프 바깥으로 이동
2. **필수**: `serviceKey`가 DB에 저장되지 않도록 params 직렬화에서 제외
3. **권장**: 세미콜론 제거, DTO 위치 정리

위 수정 후 실제 API 호출 테스트로 DB 적재 확인되면 티켓 DONE 처리 가능.
