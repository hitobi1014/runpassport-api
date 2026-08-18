package com.runpassport.runpassportapi.ingest.course

import com.runpassport.runpassportapi.course.CourseRepository
import com.runpassport.runpassportapi.ingest.dto.DurunubiPayloadItem
import com.runpassport.runpassportapi.ingest.durunubi.RawDurunubiRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import kotlin.math.log

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val rawDurunubiRepository: RawDurunubiRepository,
    private val objectMapper: ObjectMapper,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun ingestCourseFromRaw() {
        // 1. raw durunubi 전체 순회
        val rawDurunubis = rawDurunubiRepository.findAll()
        logger.info("파싱 시작 {}건", rawDurunubis.size)

        var count = 0
        rawDurunubis.mapNotNull { row ->
            // 2. raw -> JSON 파싱  region 조회
            val payloadItem = objectMapper.readValue(row.rawPayload, DurunubiPayloadItem::class.java)
            count++

            // 2-1. region에 sigun 없으면 skip -> 카운트만 증가
            if (payloadItem.sigun.isNullOrBlank()) {
                logger.info("시군구 정보 없음 skip")
                return
            }
            // 2-2. sigun 있으면 course -> existsByRawDurunubiId(두루누비ID) 체크
            if (courseRepository.existsByRawDurunubiId(row.id!!)) {
                logger.info("이미 같은 ID로 적재되어있음 skip 두루누비ID{}", row.id)
                return
            }
        }


        // 3. 엔티티 저장
        val mockList = List<DurunubiPayloadItem>()
        courseRepository.saveAll()
    }

    // GPX XML(bytes) -> (lat, lng) 좌표 목록
    private fun parseGpxCoordinates(gpxBytes: ByteArray): List<Pair<Double, Double>> {
        TODO()
        //        val gpx = ByteArrayInputStream(gpxBytes).use { input ->
//            GPX.read(input) -> read 함수 아님 알아봐야함
//        }
    }
}