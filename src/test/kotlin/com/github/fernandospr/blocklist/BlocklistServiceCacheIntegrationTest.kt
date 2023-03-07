package com.github.fernandospr.blocklist

import com.github.fernandospr.blocklist.BlocklistServiceCacheIntegrationTest.TestConfig.Companion.CACHE_NAME
import com.github.fernandospr.blocklist.BlocklistServiceCacheIntegrationTest.TestConfig.Companion.blocklist
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
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
  private lateinit var service: BlocklistService

  @Autowired
  private lateinit var cache: CacheManager

  @EnableCaching
  @TestConfiguration
  class TestConfig {

    @Bean
    fun cacheManager(): CacheManager = ConcurrentMapCacheManager(CACHE_NAME)

    @Bean
    fun service(): BlocklistService {
      val clientMock = Mockito.mock(IPSumClient::class.java)
      val ipExtractorMock = Mockito.mock(IPv4Extractor::class.java)
      val blocklistStr = blocklist.joinToString("\n")
      Mockito.`when`(clientMock.getIpBlocklist()).thenReturn(blocklistStr)
      Mockito.`when`(ipExtractorMock.fromString(blocklistStr)).thenReturn(blocklist)
      return BlocklistService(clientMock, ipExtractorMock)
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
    assertThrows(UnknownBlocklistException::class.java) { service.getIpBlocklist() }
  }

  @Test
  fun `Getting IP blocklist and updating cache should update cache`() {
    service.getIpBlocklistAndUpdateCache()

    assertEquals(blocklist, cache.getValue(CACHE_NAME))
  }

  @Test
  fun `Getting IP blocklist after updating cache should return the cached blocklist`() {
    service.getIpBlocklistAndUpdateCache()

    service.getIpBlocklist()

    assertEquals(blocklist, cache.getValue(CACHE_NAME))
  }

}

private fun CacheManager.getValue(cacheName: String) = this[cacheName]?.get(SimpleKey.EMPTY)?.get()