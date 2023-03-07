package com.github.fernandospr.blocklist

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IPv4ExtractorTest {

  private lateinit var ipExtractor: IPv4Extractor

  @BeforeEach
  fun setup() {
    ipExtractor = IPv4Extractor()
  }

  @Test
  fun `Extracting from a string that only contains a valid ip should return a list of only that valid ip`() {
    val str = "200.1.1.1"

    val result = ipExtractor.fromString(str)

    assertEquals(setOf("200.1.1.1"), result)
  }

  @Test
  fun `Extracting from a string that does not contain a valid ip should return an empty list`() {
    val str = "200.1.1.Not.a.valid.ip"

    val result = ipExtractor.fromString(str)

    assertTrue(result.isEmpty())
  }

  @Test
  fun `Extracting from a multi-line string that only contains a valid ip should return a list of only that valid ip`() {
    val str = """
      # This is a comment
      200.1.1.1
      
      # Another comment
      
      """.trimIndent()

    val result = ipExtractor.fromString(str)

    assertEquals(setOf("200.1.1.1"), result)
  }

  @Test
  fun `Extracting from a multi-line string that contains valid ips should return a list of those valid ips`() {
    val str = """
      # This is a comment
      200.1.1.1
      # Another comment
      68.9.1.2
      68.9.Hello.world
      """.trimIndent()

    val result = ipExtractor.fromString(str)

    assertEquals(setOf("200.1.1.1", "68.9.1.2"), result)
  }

  @Test
  fun `Extracting from a multi-line string (carriage return and newline) that contains valid ips should return a list of those valid ips`() {
    val str =
      "# This is a comment\r\n200.1.1.1\r\n# Another comment\r\n68.9.1.2\r\n68.9.Hello.world"

    val result = ipExtractor.fromString(str)

    assertEquals(setOf("200.1.1.1", "68.9.1.2"), result)
  }
}