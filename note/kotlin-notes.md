### Swagger DTO 샘플

- Swagger 표기 위한 DTO 작성방법

```kotlin
/**
 * Swagger DTO 샘플
 * 문서에 노출될 필드는
 * DTO의 @field:Schema — 코틀린에서 data class에 애노테이션을 붙일 때
 * @Schema만 쓰면 기본적으로 생성자 파라미터에 붙어서
 * Swagger가 못 읽는 경우가 있습니다.
 * @field: 사용 지정자를 꼭 붙여서 실제 필드에 적용되게 해야 함
 */
data class SwaggerDTOSample(
    @field:Schema(description = "코스 ID", example = "1")
    val id: Long,

    @field:Schema(description = "코스명", example = "제주 해안 5코스")
    val name: String,

    @field:Schema(description = "코스 거리(km)", example = "12.5")
    val distanceKm: Double,

    @field:Schema(description = "코스 난이도", example = "EASY", allowableValues = ["EASY", "MEDIUM", "HARD"])
    val level: String
)

```