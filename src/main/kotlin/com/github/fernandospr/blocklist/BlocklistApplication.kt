package com.github.fernandospr.blocklist

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling

@EnableCaching
@EnableScheduling
@EnableRetry
@SpringBootApplication
class BlocklistApplication

fun main(args: Array<String>) {
	runApplication<BlocklistApplication>(*args)
}
