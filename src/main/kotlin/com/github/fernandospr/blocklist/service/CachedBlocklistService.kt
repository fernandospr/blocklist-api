package com.github.fernandospr.blocklist.service

interface CachedBlocklistService {
  fun getCachedIpBlocklist(): Collection<String>
}