package com.github.fernandospr.blocklist

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class BlocklistRefresherTest {

  private lateinit var refresher: BlocklistRefresher
  private lateinit var serviceMock: BlocklistService

  @BeforeEach
  fun setup() {
    serviceMock = Mockito.mock(BlocklistService::class.java)
    Mockito.`when`(serviceMock.getIpBlocklistAndUpdateCache()).thenReturn(blocklist)
    refresher = BlocklistRefresher(serviceMock)
  }

  @Test
  fun `Refreshing on application ready should call getIpBlocklistAndUpdateCache`() {
    refresher.refreshOnApplicationReady()

    Mockito.verify(serviceMock).getIpBlocklistAndUpdateCache()
  }

  @Test
  fun `Scheduling a refresh should call getIpBlocklistAndUpdateCache`() {
    refresher.scheduleRefresh()

    Mockito.verify(serviceMock).getIpBlocklistAndUpdateCache()
  }

  companion object {
    val blocklist = setOf("1.1.1.1", "2.2.2.2", "3.3.3.3")
  }

}