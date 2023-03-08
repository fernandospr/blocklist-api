package com.github.fernandospr.blocklist.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling

@EnableCaching
@EnableScheduling
@EnableRetry
@Configuration
class BlocklistApplicationConfiguration