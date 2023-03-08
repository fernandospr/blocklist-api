package com.github.fernandospr.blocklist.service

import com.github.fernandospr.blocklist.client.IPSumClient
import com.github.fernandospr.blocklist.ipextractor.IPv4Extractor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class BlocklistServiceTest {

  private lateinit var service: BlocklistService
  private lateinit var clientMock: IPSumClient
  private lateinit var ipExtractorMock: IPv4Extractor

  @BeforeEach
  fun setup() {
    clientMock = Mockito.mock(IPSumClient::class.java)
    ipExtractorMock = Mockito.mock(IPv4Extractor::class.java)
    val blocklistStr = blocklist.joinToString("\n")
    Mockito.`when`(clientMock.getIpBlocklist()).thenReturn(blocklistStr)
    Mockito.`when`(ipExtractorMock.fromString(blocklistStr)).thenReturn(blocklist)
    service = BlocklistService(clientMock, ipExtractorMock)
  }

  @Test
  fun `Getting IP blocklist and updating cache should get blocklist from client`() {
    service.getIpBlocklistAndUpdateCache()

    Mockito.verify(clientMock).getIpBlocklist()
  }

  @Test
  fun `Getting IP blocklist and updating cache should extract ip from string`() {
    service.getIpBlocklistAndUpdateCache()

    Mockito.verify(ipExtractorMock).fromString(Mockito.anyString())
  }

  companion object {
    val blocklist = setOf("1.1.1.1", "2.2.2.2", "3.3.3.3")
  }

}