package com.github.fernandospr.blocklist.ipextractor.impl

import com.github.fernandospr.blocklist.ipextractor.IpExtractor
import org.springframework.stereotype.Component

@Component
class IPv4Extractor : IpExtractor {

  override fun fromString(str: String): Collection<String> {
    val matchResults = IP_ADDRESS_REGEX.toRegex().findAll(str)
    return matchResults.toList().map { it.value }.toHashSet()
  }

  companion object {
    /** Regular expression extracted from Android Platform
     * @see <a href="https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/util/Patterns.java;l=246?q=IP_ADDRESS&ss=android%2Fplatform%2Fsuperproject">IP_ADDRESS_STRING</a>
     */
    const val IP_ADDRESS_REGEX = "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\." +
        "(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\." +
        "(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\." +
        "(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9]))"
  }
}