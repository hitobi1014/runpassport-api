package com.runpassport.runpassportapi.ingest.tour

import com.runpassport.runpassportapi.ingest.utils.SERVICE_APP_NAME
import com.runpassport.runpassportapi.ingest.utils.normalizeToNodeList
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
        val items = mutableListOf<TourContentItem>()
        var pageNo = 1

        while (true) {
            val searchParam = LocationBasedListSearchParam(
                serviceKey = serviceKey,
                numOfRows = numOfRows,
                contentTypeId = 39,
                mobileOS = "AND",
                mobileApp = SERVICE_APP_NAME,
                // TODO 나중에 데이터 필요시 좌표값 변경 후 만들기, ex) 서울 잠수교 근처 좌표
                mapX = 126.98375,
                mapY = 37.563446,
                radius = 1000,
            )

            val endpoint = "/locationBasedList2"
            val responseBody = fetchPage(endpoint, pageNo, searchParam).path("response")
            val header = responseBody.path("header")
            val resultCode = header.path("resultCode").asString("")
            check(resultCode == "0000" || resultCode == "00") {
                "관광공사 locationBasedList2 응답 실패: resultCode=$resultCode, resultMsg=${
                    header.path("resultMsg").asString("")
                }"
            }

            val bodyNode = responseBody.path("body")
            val totalCount = bodyNode.path("totalCount").asInt(0)
            val pageItems = extractTourContent(
                bodyNode.path("items").path("item"), searchParam, endpoint
            )
            items += pageItems
            if (pageItems.isEmpty() || items.size >= totalCount || pageItems.size < numOfRows) {
                break
            }
            log.info("페이지 번호 {}", pageNo)
            pageNo++
        }
        log.info("관광공사 tourList {}건 수집 완료", items.size)
        return items;
    }

    private fun extractTourContent(
        itemNode: JsonNode,
        searchParam: LocationBasedListSearchParam,
        endPoint: String
    ): List<TourContentItem> {
        return normalizeToNodeList(itemNode).mapNotNull { node ->
            val contentId = node.path("contentid").asString("")
            if (contentId.isBlank()) {
                log.warn("contentId가 없는 코스 항목을 건너뜀: {}", node)
                null
            } else {
                TourContentItem(
                    externalId = contentId,
                    contentTypeId = searchParam.contentTypeId,
                    endpoint = endPoint,
                    params = objectMapper.writeValueAsString(searchParam),
                    rawPayload = objectMapper.writeValueAsString(node)
                )
            }
        }
    }

    /**
     * @param params: /locationBasedList2 호출 파라미터
     *              - 필수파라미터: MobileOS, MobileApp, serviceKey, mapX, mapY, radius
     */
    private fun fetchPage(endPoint: String, pageNo: Int, params: LocationBasedListSearchParam): JsonNode =
        restClient.get()
            .uri { uriBuilder ->
                uriBuilder.path(endPoint)
                    .queryParam("serviceKey", params.serviceKey)
                    .queryParam("numOfRows", params.numOfRows)
                    .queryParam("pageNo", pageNo)
                    .queryParam("MobileOS", params.mobileOS)
                    .queryParam("MobileApp", params.mobileApp)
                    .queryParam("mapX", params.mapX)
                    .queryParam("mapY", params.mapY)
                    .queryParam("radius", params.radius)
                    .queryParam("contentTypeId", params.contentTypeId)
                    .queryParam("_type", "json")
                    .build()
            }.retrieve().body(JsonNode::class.java)
            ?: error("관광공사 tour 응답 본문 비어있음 (pageNo=$pageNo)")
}

data class LocationBasedListSearchParam(
    val serviceKey: String,
    // 관광타입(12:관광지, 14:문화시설, 15:축제공연행사, 25:여행코스, 28:레포츠, 32:숙박, 38:쇼핑, 39:음식점) ID
    val contentTypeId: Int,
    val numOfRows: Int,
    val mobileOS: String,
    val mobileApp: String,
    val mapX: Double,
    val mapY: Double,
    val radius: Int,
)