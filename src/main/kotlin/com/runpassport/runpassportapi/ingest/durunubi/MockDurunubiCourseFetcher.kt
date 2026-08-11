package com.runpassport.runpassportapi.ingest.durunubi

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
@Profile("!durunubi-real")
class MockDurunubiCourseFetcher(
    private val objectMapper: ObjectMapper,
) : DurunubiCourseFetcher {

    override fun fetchCourses(): List<DurunubiCourseItem> =
        MOCK_ITEMS.map { item ->
            DurunubiCourseItem(externalId = item.crsIdx, rawPayload = objectMapper.writeValueAsString(item))
        }

    private data class MockCourseItem(
        val createdtime: String,
        val travelerinfo: String,
        val crsTourInfo: String,
        val crsSummary: String,
        val routeIdx: String,
        val crsIdx: String,
        val crsKorNm: String,
        val crsDstnc: String,
        val crsTotlRqrmHour: String,
        val modifiedtime: String,
        val sigun: String,
        val brdDiv: String,
        val gpxpath: String,
        val crsLevel: String,
        val crsCycle: String,
        val crsContents: String,
    )

    companion object {
        private val MOCK_ITEMS = listOf(
            MockCourseItem(
                createdtime = "20240101120000",
                travelerinfo = "코스 주변 편의점, 화장실 정보",
                crsTourInfo = "여의도 한강공원, 63빌딩 전망대",
                crsSummary = "한강을 따라 걷는 평탄한 강변 코스",
                routeIdx = "1",
                crsIdx = "DRNB-0001",
                crsKorNm = "한강 여의도 러닝 코스",
                crsDstnc = "5.2",
                crsTotlRqrmHour = "1.5",
                modifiedtime = "20240102120000",
                sigun = "서울특별시 영등포구",
                brdDiv = "P",
                gpxpath = "https://example.com/gpx/drnb-0001.gpx",
                crsLevel = "쉬움",
                crsCycle = "왕복",
                crsContents = "여의도 한강공원을 따라 이어지는 평탄한 강변 코스입니다.",
            ),
            MockCourseItem(
                createdtime = "20240103090000",
                travelerinfo = "코스 주변 카페, 편의점 정보",
                crsTourInfo = "해운대 해수욕장, 동백섬",
                crsSummary = "해안선을 따라 걷는 바다 전망 코스",
                routeIdx = "2",
                crsIdx = "DRNB-0002",
                crsKorNm = "해운대 해변 러닝 코스",
                crsDstnc = "7.8",
                crsTotlRqrmHour = "2.0",
                modifiedtime = "20240104090000",
                sigun = "부산광역시 해운대구",
                brdDiv = "C",
                gpxpath = "https://example.com/gpx/drnb-0002.gpx",
                crsLevel = "보통",
                crsCycle = "편도",
                crsContents = "해운대 해수욕장에서 동백섬을 잇는 해안 산책로 코스입니다.",
            ),
            MockCourseItem(
                createdtime = "20240105143000",
                travelerinfo = "코스 주변 등산로 입구, 매점 정보",
                crsTourInfo = "남산타워, 남산공원",
                crsSummary = "남산을 오르는 완만한 둘레길 코스",
                routeIdx = "3",
                crsIdx = "DRNB-0003",
                crsKorNm = "남산 둘레길 러닝 코스",
                crsDstnc = "4.3",
                crsTotlRqrmHour = "1.2",
                modifiedtime = "20240106143000",
                sigun = "서울특별시 중구",
                brdDiv = "M",
                gpxpath = "https://example.com/gpx/drnb-0003.gpx",
                crsLevel = "보통",
                crsCycle = "순환",
                crsContents = "남산공원 둘레길을 순환하는 완만한 경사의 코스입니다.",
            ),
            MockCourseItem(
                createdtime = "20240107110000",
                travelerinfo = "코스 주변 주차장, 화장실 정보",
                crsTourInfo = "경주 불국사, 첨성대",
                crsSummary = "고도 경주의 역사 유적을 잇는 평지 코스",
                routeIdx = "4",
                crsIdx = "DRNB-0004",
                crsKorNm = "경주 유적 순례 러닝 코스",
                crsDstnc = "9.6",
                crsTotlRqrmHour = "2.5",
                modifiedtime = "20240108110000",
                sigun = "경상북도 경주시",
                brdDiv = "P",
                gpxpath = "https://example.com/gpx/drnb-0004.gpx",
                crsLevel = "어려움",
                crsCycle = "편도",
                crsContents = "경주 시내 주요 유적지를 잇는 장거리 평지 코스입니다.",
            ),
        )
    }
}
