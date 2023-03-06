package com.github.fernandospr.blocklist

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class BlocklistConfiguration {
  @Bean
  fun provideRestTemplate() = RestTemplateBuilder()
    .setConnectTimeout(Duration.ofMillis(5000))
    .setReadTimeout(Duration.ofMillis(5000))
    .build()
}

