package com.runpassport.runpassportapi.ingest.tour

import com.runpassport.runpassportapi.ingest.utils.SERVICE_APP_NAME
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper

@Component
@Profile("durunubi-real")
class RealTourContentFetcher(
    restClient: RestClient.Builder,
    private val objectMapper: ObjectMapper,
    @Value("\${data.api.service-key}") private val serviceKey: String,
    @Value("\${tour.api.base-url}") private val baseUrl: String,
    @Value("\${tour.api.num-of-rows}") private val numOfRows: Int,
) : TourContentFetcher {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = restClient.clone().baseUrl(baseUrl).build()


    override fun fetchTourContent(): List<TourContentItem> {
        TODO()
//        val items = mutableListOf(TourContentItem)
//        val pageNo = 1
//
//        while (true) {
//            // TODO 응답 형태 확인 필요
//            val responseBody = fetchPage(pageNo).path("response")
//            val header = responseBody.path("header")
//                val resultCode = header.path("resultCode").asString("")
//
//
//            break;
//        }
//        return items;
    }

    /**
     * @param contentTypeId: 관광타입(12:관광지, 14:문화시설, 15:축제공연행사, 25:여행코스, 28:레포츠, 32:숙박, 38:쇼핑, 39:음식점) ID
     */
    private fun fetchPage(pageNo: Int, contentTypeId: Int = 39): JsonNode =
        restClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/locationBasedList2")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("pageNo", pageNo)
                    .queryParam("MobileOS", "AND")
                    .queryParam("MobileApp", SERVICE_APP_NAME)
                    .queryParam("contentTypeId", contentTypeId)
                    .queryParam("_type", "json")
                    .build()
            }.retrieve().body(JsonNode::class.java)
            ?: error("관광공사 tour 응답 본문 비어있음 (pageNo=$pageNo)")
}