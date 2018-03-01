package me.magicsu.onedayup

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class OneDayUpApplication

fun main(args: Array<String>) {
    SpringApplication.run(OneDayUpApplication::class.java, *args)
}
