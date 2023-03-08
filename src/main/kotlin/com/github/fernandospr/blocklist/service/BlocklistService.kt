package com.github.fernandospr.blocklist.service

import com.github.fernandospr.blocklist.ipextractor.IPv4Extractor
import com.github.fernandospr.blocklist.client.IPSumClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class BlocklistService(
  @Autowired private val client: IPSumClient,
  @Autowired private val ipExtractor: IPv4Extractor
) {

  @Cacheable(value = ["blocklist"])
  fun getIpBlocklist(): Collection<String> {
    // Throw if there's no cached blocklist
    throw UnknownBlocklistException()
  }

  @CachePut(value = ["blocklist"])
  fun getIpBlocklistAndUpdateCache(): Collection<String> {
    val blocklistStr = client.getIpBlocklist()
    return ipExtractor.fromString(blocklistStr)
  }
}