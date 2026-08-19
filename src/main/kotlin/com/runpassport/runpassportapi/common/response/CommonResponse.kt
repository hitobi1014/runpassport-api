package com.runpassport.runpassportapi.common.response

data class CommonResponse<T>(
    val data: T,
    val message: String? = null,
) {
    companion object {
        fun <T> success(data: T, message: String? = null): CommonResponse<T> = CommonResponse(data, message)

    }
}
