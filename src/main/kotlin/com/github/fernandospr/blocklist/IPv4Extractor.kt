package com.github.fernandospr.blocklist

import org.springframework.stereotype.Component

@Component
class IPv4Extractor {

  fun fromString(str: String): Collection<String> {
    val matchResults = IP_ADDRESS_REGEX.toRegex().findAll(str)
    return matchResults.toList().map { it.value }.toHashSet()
  }

  companion object {
    const val IP_ADDRESS_REGEX = "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\." +
        "(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\." +
        "(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\." +
        "(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9]))"
  }
}