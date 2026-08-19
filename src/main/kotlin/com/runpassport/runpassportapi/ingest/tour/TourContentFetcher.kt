package com.runpassport.runpassportapi.ingest.tour

interface TourContentFetcher {
    fun fetchTourContent(): List<TourContentItem>
}

data class TourContentItem(
    val externalId: String,
    val contentTypeId: Int,
    val endpoint: String,
    val params: String,
    val rawPayload: String,
)
