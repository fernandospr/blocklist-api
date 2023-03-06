package com.github.fernandospr.blocklist

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
class BlocklistController(
  @Autowired private val service: BlocklistService
) {

  @GetMapping("/ips/{ip}")
  fun isIpInBlacklist(
    @PathVariable("ip")
    ip: String
  ): String {
    return service.getIpBlocklist().contains(ip).toString()
  }
}