package com.github.fernandospr.blocklist

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class BlocklistService(
  @Autowired private val client: IPSumClient,
  @Autowired private val ipExtractor: Ipv4Extractor
) {

  @Cacheable(value = ["blocklist"])
  fun getIpBlocklist(): Collection<String> {
    val blocklistStr = client.getIpBlocklist()
    return ipExtractor.fromString(blocklistStr)
  }

  @CachePut(value = ["blocklist"])
  fun getIpBlocklistAndUpdateCache(): Collection<String> {
    val blocklistStr = client.getIpBlocklist()
    return ipExtractor.fromString(blocklistStr)
  }
}