package com.runpassport.runpassportapi.ingest.durunubi

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(name = "raw_durunubi")
class RawDurunubi(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @Column(name = "external_id", nullable = false, unique = true)
    val externalId: String,

    @Column(name = "raw_payload", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    val rawPayload: String,

    @Column(name = "collected_at", nullable = false)
    val collectedAt: Instant = Instant.now(),
)
