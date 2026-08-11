package com.runpassport.runpassportapi.ingest.durunubi

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper

@Component
@Profile("durunubi-real")
class RealDurunubiCourseFetcher(
    restClientBuilder: RestClient.Builder,
    private val objectMapper: ObjectMapper,
    @Value("\${durunubi.api.base-url}") private val baseUrl: String,
    @Value("\${durunubi.api.service-key}") private val serviceKey: String,
    @Value("\${durunubi.api.num-of-rows:1000}") private val numOfRows: Int,
) : DurunubiCourseFetcher {

    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = restClientBuilder.baseUrl(baseUrl).build()

    /**
     * 빈이 생성되는 시점(= durunubi-real 프로파일로 기동될 때) 딱 한 번 실행되는 검증.
     * 설정이 잘못된 채로 배치를 돌려서 API 호출 단계에서야 401로 뒤늦게 알게 되는 걸 방지.
     */
    @PostConstruct
    fun validateConfig() {
        check(serviceKey.isNotBlank()) {
            "durunubi.api.service-key 가 비어있습니다. " +
                    "환경변수 DURUNUBI_API_SERVICE_KEY 가 실제로 주입됐는지 확인하세요."
        }
        log.info(
            "[두루누비 API 설정 확인] base-url={}, num-of-rows={}, service-key={}",
            baseUrl,
            numOfRows,
            maskKey(serviceKey)
        )
    }

    /** 키 전문은 로그에 남기지 않고, 앞 4자리만 보여주고 나머지는 마스킹 */
    private fun maskKey(key: String): String {
        if (key.length <= 4) return "*".repeat(key.length)
        return key.take(4) + "*".repeat(key.length - 4)
    }

    override fun fetchCourses(): List<DurunubiCourseItem> {
        val items = mutableListOf<DurunubiCourseItem>()
        var pageNo = 1

        while (true) {
            val responseBody = fetchPage(pageNo).path("response")
            val header = responseBody.path("header")
            val resultCode = header.path("resultCode").asString("")
            check(resultCode == "0000" || resultCode == "00") {
                "두루누비 courseList 응답 실패: resultCode=$resultCode, resultMsg=${header.path("resultMsg").asString("")}"
            }

            val bodyNode = responseBody.path("body")
            val totalCount = bodyNode.path("totalCount").asInt(0)
            val pageItems = extractItems(bodyNode.path("items").path("item"))
            items += pageItems

            if (pageItems.isEmpty() || items.size >= totalCount || pageItems.size < numOfRows) {
                break
            }
            log.info("페이지 번호 {}", pageNo)
            pageNo++
        }

        log.info("두루누비 courseList {}건 수집 완료", items.size)
        return items
    }

    private fun fetchPage(pageNo: Int): JsonNode =
        restClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/courseList")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("pageNo", pageNo)
                    .queryParam("MobileOS", "AND") // AND=안드로이드, IOS=아이폰, WIN=윈도우폰, ETC
                    .queryParam("MobileApp", "runpassport")
                    .queryParam("_type", "json")
                    .build()
            }.retrieve().body(JsonNode::class.java)
            ?: error("두루누비 courseList 응답 본문이 비어있음 (pageNo=$pageNo)")

    private fun extractItems(itemNode: JsonNode): List<DurunubiCourseItem> {
        if (itemNode.isMissingNode || itemNode.isNull) return emptyList()

        // 공공데이터 API 흔한 패턴: item이 배열일 수도, 단일 객체일 수도 있음 — 둘 다 방어적으로 처리.
        val nodes: List<JsonNode> = when {
            itemNode.isArray -> itemNode.toList()
            itemNode.isObject -> listOf(itemNode)
            else -> emptyList()
        }

        return nodes.mapNotNull { node ->
            val crsIdx = node.path("crsIdx").asString("")
            if (crsIdx.isBlank()) {
                log.warn("crsIdx가 없는 코스 항목을 건너뜀: {}", node)
                null
            } else {
                DurunubiCourseItem(externalId = crsIdx, rawPayload = objectMapper.writeValueAsString(node))
            }
        }
    }

    // TODO: 길 목록 정보조회(`/routeList`)는 필요해지면 fetchPage와 동일한 페이징/파싱 패턴으로
    //       별도 메서드(또는 별도 클래스)로 추가한다. 지금은 courseList만 구현.
}
