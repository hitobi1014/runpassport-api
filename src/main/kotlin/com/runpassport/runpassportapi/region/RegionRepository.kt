package com.runpassport.runpassportapi.region

import org.springframework.data.jpa.repository.JpaRepository

interface RegionRepository : JpaRepository<Region, Long> {
    fun findBySigun(sigun: String): List<Region>
}