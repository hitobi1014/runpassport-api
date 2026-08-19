package com.runpassport.runpassportapi.course

import com.runpassport.runpassportapi.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "courses")
class Course(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @Column(name = "raw_durunubi_id", nullable = false)
    val rawDurunubiId: Long,

    @Column(name = "region_id", nullable = false)
    val regionId: Long,

    @Column(name = "name", nullable = false)
    val name: String, // 코스명

    @Column(name = "distance_m")
    val distanceM: Int, // 총 거리 (단위 m)

    @Column(name = "difficulty")
    val difficulty: String, // 난이도

    @Column(name = "terrain_type")
    val terrainType: String?, // 지형(강변/해안/공원) 등

    @Column(name = "gpx_storage_ref")
    val gpxStorageRef: String, // gpx 파일 참조 => durunubi
) : BaseEntity()
