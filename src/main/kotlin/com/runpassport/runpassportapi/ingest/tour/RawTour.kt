package com.runpassport.runpassportapi.ingest.tour

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant

@Entity
@Table(name = "raw_tour")
class RawTour(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @Column(name = "external_id", nullable = false, unique = true)
    val externalId: String,

    @Column(name = "content_type_id", nullable = false, unique = true)
    val contentTypeId: Int,

    @Column(name = "endpoint", nullable = false)
    val endpoint: String,

    @Column(name = "params", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    val params: String,

    @Column(name = "raw_payload", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    val rawPayload: String,

    @Column(name = "collected_at", nullable = false)
    val collectedAt: Instant = Instant.now(),
)