package com.github.fernandospr.blocklist.controller

import com.github.fernandospr.blocklist.ipextractor.impl.IPv4Extractor.Companion.IP_ADDRESS_REGEX
import com.github.fernandospr.blocklist.service.CachedBlocklistService
import jakarta.validation.constraints.Pattern
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
@Validated
class BlocklistController(
  @Autowired private val service: CachedBlocklistService
) {

  @GetMapping("/ips/{ip}")
  fun isIpInBlocklist(
    @Pattern(regexp = IP_ADDRESS_REGEX, message = "Invalid IPv4 address")
    @PathVariable("ip")
    ip: String
  ): String {
    return service.getCachedIpBlocklist().contains(ip).toString()
  }
}