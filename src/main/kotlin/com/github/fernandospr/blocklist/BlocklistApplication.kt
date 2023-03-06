package com.github.fernandospr.blocklist

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableCaching
@SpringBootApplication
class BlocklistApplication

fun main(args: Array<String>) {
	runApplication<BlocklistApplication>(*args)
}
