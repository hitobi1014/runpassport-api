package com.runpassport.runpassportapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class RunpassportApiApplication

fun main(args: Array<String>) {
    runApplication<RunpassportApiApplication>(*args)
}
