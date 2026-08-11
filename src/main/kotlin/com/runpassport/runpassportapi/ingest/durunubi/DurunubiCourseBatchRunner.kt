package com.runpassport.runpassportapi.ingest.durunubi

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("batch-durunubi")
class DurunubiCourseBatchRunner(
    private val fetcher: DurunubiCourseFetcher,
    private val ingestService: DurunubiCourseIngestService,
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String) {
        log.info("두루누비 코스목록 배치 시작")

        val items = fetcher.fetchCourses()
        if (items.isEmpty()) {
            log.warn("두루누비 courseList 응답이 비어있어 raw_durunubi 적재를 건너뜀 (기존 데이터 유지)")
            return
        }

        ingestService.replaceAll(items)
        log.info("두루누비 코스목록 {}건 적재 완료", items.size)
    }
}
