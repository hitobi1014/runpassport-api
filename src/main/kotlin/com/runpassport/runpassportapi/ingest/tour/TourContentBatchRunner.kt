package com.runpassport.runpassportapi.ingest.tour

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("batch-durunubi")
class TourContentBatchRunner(
    private val fetcher: TourContentFetcher,
    private val ingestService: TourContentIngestService,
) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String) {
        log.info("관광공사 배치 시작")

        val items = fetcher.fetchTourContent()
        if (items.isEmpty()) {
            log.warn("관광 tour 응답이 비어있어 raw_tour 적재 건너뜀 (기존 데이터 유지)")
            return
        }

        ingestService.replaceAll(items)
        log.info("관광 tour 목록 {}건 적재 완료", items.size)
    }
}