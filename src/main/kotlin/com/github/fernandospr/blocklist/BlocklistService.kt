package com.github.fernandospr.blocklist

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class BlocklistService(
  @Autowired private val client: IPSumClient,
  @Autowired private val ipExtractor: Ipv4Extractor
) {

  fun getIpBlocklist(): Collection<String> {
    val blocklistStr = client.getIpBlocklist()
    return ipExtractor.fromString(blocklistStr)
  }
}