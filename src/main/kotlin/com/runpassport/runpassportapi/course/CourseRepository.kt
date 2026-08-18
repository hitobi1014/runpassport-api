package com.runpassport.runpassportapi.course

import org.springframework.data.jpa.repository.JpaRepository

interface CourseRepository : JpaRepository<Course, Long> {
    fun existsByRawDurunubiId(rawDurunubiId: Long): Boolean
}