package net.stepniak.morenomodels.service.springapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
class SpringApiApplication

fun main(args: Array<String>) {
    runApplication<SpringApiApplication>(*args)
}
