package com.github.fernandospr.blocklist.controller

import com.github.fernandospr.blocklist.service.BlocklistService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class BlocklistControllerTest {

  private lateinit var controller: BlocklistController
  private lateinit var serviceMock: BlocklistService

  @BeforeEach
  fun setup() {
    serviceMock = Mockito.mock(BlocklistService::class.java)
    controller = BlocklistController(serviceMock)
  }

  @Test
  fun `isIpInBlocklist of an ip should return true when service returns a blocklist containing the ip`() {
    Mockito.`when`(serviceMock.getIpBlocklist()).thenReturn(setOf("2.2.2.2", "1.1.1.1", "3.3.3.3"))

    val result = controller.isIpInBlocklist("1.1.1.1")

    assertTrue(result.toBoolean())
  }

  @Test
  fun `isIpInBlocklist of an ip should return false when service returns a blocklist without the ip`() {
    Mockito.`when`(serviceMock.getIpBlocklist()).thenReturn(setOf("2.2.2.2"))

    val result = controller.isIpInBlocklist("1.1.1.1")

    assertFalse(result.toBoolean())
  }

}