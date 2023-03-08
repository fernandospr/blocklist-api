package com.github.fernandospr.blocklist.service.impl

import com.github.fernandospr.blocklist.client.RemoteBlocklistClient
import com.github.fernandospr.blocklist.ipextractor.IpExtractor
import com.github.fernandospr.blocklist.service.CacheUpdaterBlocklistService
import com.github.fernandospr.blocklist.service.CachedBlocklistService
import com.github.fernandospr.blocklist.service.UnknownBlocklistException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class BlocklistService(
  @Autowired private val client: RemoteBlocklistClient,
  @Autowired private val ipExtractor: IpExtractor
) : CachedBlocklistService, CacheUpdaterBlocklistService {

  @Cacheable(value = ["blocklist"])
  override fun getCachedIpBlocklist(): Collection<String> {
    // Throw if there's no cached blocklist
    throw UnknownBlocklistException()
  }

  @CachePut(value = ["blocklist"])
  override fun getIpBlocklistAndUpdateCache(): Collection<String> {
    val blocklistStr = client.getIpBlocklist()
    return ipExtractor.fromString(blocklistStr)
  }
}