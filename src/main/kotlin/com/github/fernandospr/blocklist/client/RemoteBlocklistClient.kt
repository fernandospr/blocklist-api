package com.github.fernandospr.blocklist.client

interface RemoteBlocklistClient {
  fun getIpBlocklist(): String
}