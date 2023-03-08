package com.github.fernandospr.blocklist.service.impl

import com.github.fernandospr.blocklist.client.RemoteBlocklistClient
import com.github.fernandospr.blocklist.ipextractor.IpExtractor
import com.github.fernandospr.blocklist.service.CacheUpdaterBlocklistService
import com.github.fernandospr.blocklist.service.CachedBlocklistService
import com.github.fernandospr.blocklist.service.UnknownBlocklistException
import com.github.fernandospr.blocklist.service.impl.BlocklistServiceCacheIntegrationTest.TestConfig.Companion.CACHE_NAME
import com.github.fernandospr.blocklist.service.impl.BlocklistServiceCacheIntegrationTest.TestConfig.Companion.blocklist
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.cache.get
import org.springframework.cache.interceptor.SimpleKey
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
class BlocklistServiceCacheIntegrationTest {

  @Autowired
  private lateinit var cachedBlocklistService: CachedBlocklistService

  @Autowired
  private lateinit var cacheUpdaterBlocklistService: CacheUpdaterBlocklistService

  @Autowired
  private lateinit var cache: CacheManager

  @EnableCaching
  @TestConfiguration
  class TestConfig {

    private val service by lazy {
      val clientMock = Mockito.mock(RemoteBlocklistClient::class.java)
      val ipExtractorMock = Mockito.mock(IpExtractor::class.java)
      val blocklistStr = blocklist.joinToString("\n")
      Mockito.`when`(clientMock.getIpBlocklist()).thenReturn(blocklistStr)
      Mockito.`when`(ipExtractorMock.fromString(blocklistStr)).thenReturn(blocklist)
      BlocklistService(clientMock, ipExtractorMock)
    }

    @Bean
    fun cacheManager(): CacheManager = ConcurrentMapCacheManager(CACHE_NAME)

    @Bean
    fun cachedBlocklistService(): CachedBlocklistService {
      return service
    }

    @Bean
    fun cacheUpdaterBlocklistService(): CacheUpdaterBlocklistService {
      return service
    }

    companion object {
      val blocklist = setOf("1.1.1.1", "2.2.2.2", "3.3.3.3")
      const val CACHE_NAME = "blocklist"
    }
  }

  @BeforeEach
  fun setup() {
    cache[CACHE_NAME]?.clear()
  }

  @Test
  fun `Getting IP blocklist without cache should throw exception`() {
    Assertions.assertThrows(UnknownBlocklistException::class.java) { cachedBlocklistService.getCachedIpBlocklist() }
  }

  @Test
  fun `Getting IP blocklist and updating cache should update cache`() {
    cacheUpdaterBlocklistService.getIpBlocklistAndUpdateCache()

    Assertions.assertEquals(blocklist, cache.getValue(CACHE_NAME))
  }

  @Test
  fun `Getting IP blocklist after updating cache should return the cached blocklist`() {
    cacheUpdaterBlocklistService.getIpBlocklistAndUpdateCache()

    cachedBlocklistService.getCachedIpBlocklist()

    Assertions.assertEquals(blocklist, cache.getValue(CACHE_NAME))
  }

  private fun CacheManager.getValue(cacheName: String) =
    this[cacheName]?.get(SimpleKey.EMPTY)?.get()

}