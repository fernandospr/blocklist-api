package com.github.fernandospr.blocklist.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.Duration

/**
 * RestTemplate connect timeout of 5000ms by default.
 */
const val BLOCKLIST_REST_CLIENT_CONNECTIMEOUT_DEFAULT = 5000

/**
 * RestTemplate read timeout of 5000ms by default.
 */
const val BLOCKLIST_REST_CLIENT_READTIMEOUT_DEFAULT = 5000

@EnableCaching
@EnableScheduling
@EnableRetry
@Configuration
class BlocklistConfiguration(
  @Value("\${blocklist.rest.client.connectTimeout:$BLOCKLIST_REST_CLIENT_CONNECTIMEOUT_DEFAULT}")
  private val restClientConnectTimeout: Long,
  @Value("\${blocklist.rest.client.readTimeout:$BLOCKLIST_REST_CLIENT_READTIMEOUT_DEFAULT}")
  private val restClientReadTimeout: Long
) {
  @Bean
  fun provideRestTemplate() = RestTemplateBuilder()
    .setConnectTimeout(Duration.ofMillis(restClientConnectTimeout))
    .setReadTimeout(Duration.ofMillis(restClientReadTimeout))
    .build()
}

