package com.adrianmalmierca.dijonevents

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching //enable the caché for the event caching
class DijonEventsApplication

fun main(args: Array<String>) {
    runApplication<DijonEventsApplication>(*args)
}
