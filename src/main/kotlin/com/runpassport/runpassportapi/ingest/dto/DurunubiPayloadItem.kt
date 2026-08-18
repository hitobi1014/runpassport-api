package com.runpassport.runpassportapi.ingest.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)  // API가 필드 추가해도 파싱 안 깨지게
data class DurunubiPayloadItem(
    val sigun: String?, // 행정구역, 시군구 ex) 경남 밀양시
    val brdDiv: String?, // 걷기/자전거 구분 ex) DNWW: 걷기 길, DNBW: 자전거 길
    val crsIdx: String?, // 코스 고유번호 ex)T_CRS_MNG0000005461
    val gpxpath: String?, // GPX 경로
    val crsCycle: String?, // 순환형태 ex) 비순환형
    val crsDstnc: String?, // 코스길이 (단위: km) ex) 10
    val crsLevel: String?, // 난이도 (1: 하/2: 중/3: 상)
    val crsTotlRqrmHour: String?, // 총 소요시간 (단위: 분) ex) 50
    val crsKorNm: String?, // 코스명  ex) 밀양강 자전거길
    val routeIdx: String?, // 길 고유번호 (길에 코스 존재하지 않을 수 있음) ex)T_ROUTE_MNG00000006
    val crsSummary: String?, // 코스 개요
    val crsContents: String?, // 코스 설명
    val crsTourInfo: String?, // 관광 포인트
    val travelerinfo: String?, // 여행자 정보
    val createdtime: String?, // 등록일 ex)20210409043400
    val modifiedtime: String?, // 수정일 ex)20210409043525
)