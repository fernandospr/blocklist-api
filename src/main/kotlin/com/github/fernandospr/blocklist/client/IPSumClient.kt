package com.github.fernandospr.blocklist.client

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

/**
 * Retry 5 times by default.
 */
const val BLOCKLIST_REST_CLIENT_RETRY_MAXATTEMPTS_DEFAULT = 5

/**
 * Use a backoff delay of 5000ms by default.
 */
const val BLOCKLIST_REST_CLIENT_RETRY_DELAY_DEFAULT = 5000

/**
 * Use a backoff multiplier of 2.0 by default.
 */
const val BLOCKLIST_REST_CLIENT_RETRY_MULTIPLIER_DEFAULT = 2.0

@Component
class IPSumClient(
  @Autowired private val restTemplate: RestTemplate
) {

  @Retryable(
    maxAttemptsExpression = "#{\${blocklist.rest.client.retry.maxAttempts:$BLOCKLIST_REST_CLIENT_RETRY_MAXATTEMPTS_DEFAULT}}",
    backoff = Backoff(
      delayExpression = "#{\${blocklist.rest.client.retry.backoff.delay:$BLOCKLIST_REST_CLIENT_RETRY_DELAY_DEFAULT}}",
      multiplierExpression = "#{\${blocklist.rest.client.retry.backoff.multiplier:$BLOCKLIST_REST_CLIENT_RETRY_MULTIPLIER_DEFAULT}}"
    )
  )
  fun getIpBlocklist(): String {
    val result = restTemplate.getForEntity(url, String::class.java)
    return result.body.orEmpty()
  }

  private companion object {
    const val url = "https://raw.githubusercontent.com/stamparm/ipsum/master/ipsum.txt"
  }
}