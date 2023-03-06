package com.github.fernandospr.blocklist

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class IPSumClient(
  @Autowired private val restTemplate: RestTemplate
) {

  fun getIpBlocklist(): String {
    val result = restTemplate.getForEntity(url, String::class.java)
    return result.body.orEmpty()
  }

  private companion object {
    const val url = "https://raw.githubusercontent.com/stamparm/ipsum/master/ipsum.txt"
  }
}