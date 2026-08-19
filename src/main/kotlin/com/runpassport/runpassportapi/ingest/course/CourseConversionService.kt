package com.runpassport.runpassportapi.ingest.course

import com.runpassport.runpassportapi.course.Course
import com.runpassport.runpassportapi.course.CourseRepository
import com.runpassport.runpassportapi.ingest.dto.DurunubiPayloadItem
import com.runpassport.runpassportapi.ingest.durunubi.RawDurunubiRepository
import com.runpassport.runpassportapi.region.RegionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import kotlin.math.roundToInt

@Service
class CourseConversionService(
    private val courseRepository: CourseRepository,
    private val regionRepository: RegionRepository,
    private val rawDurunubiRepository: RawDurunubiRepository,
    private val objectMapper: ObjectMapper,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun ingestCourseFromRaw() {
        // 1. raw durunubi 전체 순회
        val rawDurunubis = rawDurunubiRepository.findAll()
        logger.info("파싱 시작 {}건", rawDurunubis.size)

        var count = 0
        var skippedNoSigun = 0
        var skippedDuplicate = 0
        var skippedByRegion = 0
        var skippedMissingField = 0
        val courses = rawDurunubis.mapNotNull { row ->
            // 2. raw -> JSON 파싱  region 조회
            val payloadItem = objectMapper.readValue(row.rawPayload, DurunubiPayloadItem::class.java)
            count++

            // 2-1. region에 sigun 없으면 skip -> 카운트만 증가
            if (payloadItem.sigun.isNullOrBlank()) {
                skippedNoSigun++
                logger.info("시군구 정보 없음 skip")
                return@mapNotNull null
            }
            // 2-2. sigun 있으면 course -> existsByRawDurunubiId(두루누비ID) 체크
            if (courseRepository.existsByRawDurunubiId(row.id!!)) {
                skippedDuplicate++
                logger.info("이미 같은 ID로 적재되어있음 skip 두루누비ID{}", row.id)
                return@mapNotNull null
            }

            // 3. regionId
            val regions = regionRepository.findBySigun(payloadItem.sigun).firstOrNull()
                ?: run { skippedByRegion++; return@mapNotNull null }

            // 4. courses row를 구성할 필수 필드(이름/난이도/gpx경로)가 비어있으면 skip -> 카운트만 증가
            val name = payloadItem.crsKorNm
            val difficulty = payloadItem.crsLevel
            val gpxRef = payloadItem.gpxpath
            if (name.isNullOrBlank() || difficulty.isNullOrBlank() || gpxRef.isNullOrBlank()) {
                skippedMissingField++
                logger.info("필수 필드 누락으로 skip 두루누비ID{}", row.id)
                return@mapNotNull null
            }

            val course = regions.id?.let { regionId ->
                payloadItem.toEntity(row.id!!, regionId, name, difficulty, gpxRef)
            }
            return@mapNotNull course
        }
        // 3. 엔티티 저장
        courseRepository.saveAll(courses)
        logger.info(
            "총 시도: {}건, 시군구없음 skip: {}건, 중복 skip: {}건, region 매칭실패 skip: {}건, 필수필드누락 skip: {}건, 변환 진행: {}건",
            count, skippedNoSigun, skippedDuplicate, skippedByRegion, skippedMissingField, courses.size,
        )
    }

    private fun DurunubiPayloadItem.toEntity(
        durunubiId: Long,
        regionId: Long,
        name: String,
        difficulty: String,
        gpxStorageRef: String,
    ) = Course(
        rawDurunubiId = durunubiId,
        regionId = regionId,
        name = name,
        distanceM = crsDstnc
            ?.toDoubleOrNull()
            ?.let { km -> (km * 1000).roundToInt() } // km -> m 변환
            ?: 0,  // 값 없으면 0 반환
        difficulty = difficulty,
        gpxStorageRef = gpxStorageRef,
        terrainType = null
    )

    // GPX XML(bytes) -> (lat, lng) 좌표 목록
    private fun parseGpxCoordinates(gpxBytes: ByteArray): List<Pair<Double, Double>> {
        TODO()
        //        val gpx = ByteArrayInputStream(gpxBytes).use { input ->
//            GPX.read(input) -> read 함수 아님 알아봐야함
//        }
    }
}