package com.runpassport.runpassportapi.ingest.durunubi

interface DurunubiCourseFetcher {
    fun fetchCourses(): List<DurunubiCourseItem>

    // TODO: 길 목록 정보조회(`/routeList`)가 필요해지면 fetchRoutes(): List<DurunubiRouteItem> 를
    //       이 인터페이스(또는 별도 DurunubiRouteFetcher)에 추가한다. courseList와 동일하게
    //       페이징 + item 배열/단일객체 방어 파싱 패턴을 재사용하면 된다. 지금은 미구현.
}

// rawPayload: item 하나의 원본 JSON 전체 텍스트 (raw_durunubi.raw_payload에 그대로 저장됨)
data class DurunubiCourseItem(
    val externalId: String,
    val rawPayload: String,
)
