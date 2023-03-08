package com.github.fernandospr.blocklist.controller

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ControllerExceptionHandler {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(ConstraintViolationException::class)
  fun exceptionHandler(e: ConstraintViolationException) =
    e.constraintViolations.firstOrNull()?.message
}