package com.runpassport.runpassportapi.ingest.course

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("batch-course-convert")
class CourseConversionBatchRunner(
    private val courseService: CourseConversionService,
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String) {
        log.info("코스 목록 가공 배치 시작")
        courseService.ingestCourseFromRaw()
        log.info("코스 가공 배치 종료")
    }
}