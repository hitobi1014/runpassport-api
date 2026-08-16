package com.runpassport.runpassportapi.ingest.utils

import tools.jackson.databind.JsonNode

// 관광공사 API 전달파라미터: MobileApp 서비스명(어플명)
val SERVICE_APP_NAME = "runpassport"

/** 키 전문은 로그에 남기지 않고, 앞 4자리만 보여주고 나머지는 마스킹 */
fun maskKey(key: String): String {
    if (key.length <= 4) return "*".repeat(key.length)
    return key.take(4) + "*".repeat(key.length - 4)
}

fun normalizeToNodeList(node: JsonNode): List<JsonNode> {
    if (node.isMissingNode || node.isNull) return emptyList()
    // 공공데이터 API 흔한 패턴: item이 배열일 수도, 단일 객체일 수도 있음 — 둘 다 방어적으로 처리.
    return when {
        node.isArray -> node.toList()
        node.isObject -> listOf(node)
        else -> emptyList()
    }
}
