package com.runpassport.runpassportapi.ingest.tour

import org.springframework.data.jpa.repository.JpaRepository

interface RawTourRepository : JpaRepository<RawTour, Long>