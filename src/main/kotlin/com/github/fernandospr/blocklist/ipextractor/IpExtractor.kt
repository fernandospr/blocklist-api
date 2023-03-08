package com.github.fernandospr.blocklist.ipextractor

interface IpExtractor {
  fun fromString(str: String): Collection<String>
}