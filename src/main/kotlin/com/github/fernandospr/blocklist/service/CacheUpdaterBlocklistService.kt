package com.github.fernandospr.blocklist.service

interface CacheUpdaterBlocklistService {
  fun getIpBlocklistAndUpdateCache(): Collection<String>
}
