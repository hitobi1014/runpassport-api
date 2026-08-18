package com.runpassport.runpassportapi.region

import com.runpassport.runpassportapi.common.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "regions")
class Region(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column("id")
    val id: Long? = null,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "center_lat", nullable = false, precision = 9, scale = 6)
    val centerLat: BigDecimal,

    @Column(name = "center_lng", nullable = false, precision = 9, scale = 6)
    val centerLng: BigDecimal,

    @Column(name = "sigun", nullable = false)
    val sigun: String,
) : BaseEntity()
