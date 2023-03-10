package com.github.fernandospr.blocklist.service

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
class UnknownBlocklistException : RuntimeException()