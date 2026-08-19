package com.runpassport.runpassportapi.ingest.dto

import com.fasterxml.jackson.annotation.JsonIgnore

data class LocationBasedListSearchParam(
    @JsonIgnore
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
