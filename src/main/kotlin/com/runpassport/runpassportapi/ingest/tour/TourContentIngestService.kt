package com.runpassport.runpassportapi.ingest.tour

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TourContentIngestService(
    private val repository: RawTourRepository
) {
    // items가 비어있으면 아무 것도 하지 않는다 — 호출부(Runner)에서도 먼저 걸러내지만,
    // API 실패/빈 응답 상태에서 기존 데이터가 삭제되는 사고를 막기 위한 이중 방어.
    @Transactional
    fun replaceAll(items: List<TourContentItem>) {
        if (items.isEmpty()) return

        repository.deleteAllInBatch()
        repository.saveAll(items.map { it.toEntity() })
    }

    private fun TourContentItem.toEntity() = RawTour(
        externalId = externalId,
        endpoint = endpoint,
        contentTypeId = contentTypeId,
        params = params,
        rawPayload = rawPayload,
    )
}