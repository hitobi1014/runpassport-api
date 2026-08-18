# 요청 응답: jpx로 GPX XML 파싱하는 예시 코드

- 작성일: 2026-08-18
- 요청: `io.jenetics:jpx:3.2.1`(이미 `build.gradle.kts`에 추가돼 있음)로 GPX XML을 파싱하는 방법,
  예시 코드만.
- **주의**: 아래 `GPX`/`Track`/`TrackSegment`/`WayPoint` 메서드명은 기억 기반으로 작성한 것이라,
  실제 jpx 3.2.1 API와 미세하게 다를 수 있음 — 컴파일 에러 나면 IDE 자동완성으로 정확한
  메서드명(특히 `getLatitude()`/`latitude()` 같은 getter 스타일 차이)을 확인해줘. 이 프로젝트가
  이전에도 "문서/기억과 실제 응답이 달랐던" 경험이 있어서(두루누비 API), 이것도 같은 종류의
  리스크로 보고 검증 없이 그대로 믿지 않는 게 안전함.

---

## 예시 코드

```kotlin
import io.jenetics.jpx.GPX
import java.io.ByteArrayInputStream

// GPX XML(bytes) -> (lat, lng) 좌표 목록
fun parseGpxCoordinates(gpxBytes: ByteArray): List<Pair<Double, Double>> {
    val gpx = ByteArrayInputStream(gpxBytes).use { input ->
        GPX.read(input)
    }

    return gpx.tracks()
        .flatMap { track -> track.segments() }
        .flatMap { segment -> segment.points() }
        .map { point -> point.latitude.toDegrees() to point.longitude.toDegrees() }
        .toList()
}
```

`raw_durunubi.raw_payload ->> 'gpxpath'`가 외부 URL이라, 실제로는 먼저 HTTP로 받아와야 함
(프로젝트에 이미 있는 `RestClient.Builder` 패턴 재사용, `RealDurunubiCourseFetcher.kt` 참고):

```kotlin
import org.springframework.web.client.RestClient

fun fetchGpxBytes(restClient: RestClient, gpxUrl: String): ByteArray =
    restClient.get()
        .uri(gpxUrl)
        .retrieve()
        .body(ByteArray::class.java)
        ?: error("GPX 응답이 비어있음: $gpxUrl")
```

두 개 합치면:

```kotlin
val coordinates = parseGpxCoordinates(fetchGpxBytes(restClient, gpxUrl))
// coordinates: List<Pair<Double, Double>>  (lat, lng)
```

`note/gpx-tech.md`에서 설계한 "코스 하나의 trkpt 평균 → 코스 중심좌표" 계산은 이 `coordinates`
리스트를 그대로 산술평균 내면 됨:

```kotlin
val centerLat = coordinates.map { it.first }.average()
val centerLng = coordinates.map { it.second }.average()
```

---

## 참고 (요청 범위 밖, 짧게만 언급)

코드 훑어보다가 눈에 띈 것 2개, 지금 요청과는 별개라 고치지 않고 알려만 둠:

- `Course.kt`의 `@Table(name = "course")`가 단수형인데, 실제 DB 테이블은 `courses`(복수형) —
  지금 상태로 실행하면 테이블을 못 찾을 것으로 보임.
- `CourseService.convertXml(): DurunubiPayloadItem` 시그니처가 XML을 파싱해서 다시
  `DurunubiPayloadItem`(JSON payload용 DTO)을 반환하는 모양인데, GPX 파싱 결과는 좌표 목록이라
  이 리턴 타입과는 안 맞아 보임 — 위 예시의 `List<Pair<Double, Double>>` 같은 별도 반환 타입이
  필요할 것 같음.
