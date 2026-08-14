package com.runpassport.runpassportapi.ingest.utils

// 관광공사 API 전달파라미터: MobileApp 서비스명(어플명)
val SERVICE_APP_NAME = "runpassport"

/** 키 전문은 로그에 남기지 않고, 앞 4자리만 보여주고 나머지는 마스킹 */
fun maskKey(key: String): String {
    if (key.length <= 4) return "*".repeat(key.length)
    return key.take(4) + "*".repeat(key.length - 4)
}
