package com.github.fernandospr.blocklist

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
class BlocklistController {

  @GetMapping("/ips/{ip}")
  fun isIpInBlacklist(
    @PathVariable("ip")
    ip: String
  ): String {
    return ip.endsWith("1").toString()
  }
}