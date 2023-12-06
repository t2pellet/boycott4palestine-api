package com.t2pellet.boycottisraelapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BoycottApp

fun main(args: Array<String>) {
    runApplication<BoycottApp>(*args)
}
