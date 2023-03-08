package com.github.fernandospr.blocklist.refresher

import com.github.fernandospr.blocklist.service.BlocklistService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Refresh at 00:00:00 everyday by default.
 */
const val BLOCKLIST_REFRESHER_CRON_DEFAULT = "0 0 0 * * *"

@Component
class BlocklistRefresher(
  @Autowired private val service: BlocklistService,
  private val logger: Logger = LoggerFactory.getLogger(BlocklistRefresher::class.java)
) {

  @EventListener(ApplicationReadyEvent::class)
  fun refreshOnApplicationReady() {
    refreshBlocklistCache()
  }

  @Scheduled(cron = "\${blocklist.refresher.cron:$BLOCKLIST_REFRESHER_CRON_DEFAULT}")
  fun scheduleRefresh() = refreshBlocklistCache()

  private fun refreshBlocklistCache() {
    try {
      logger.info("Refreshing the blocklist...")
      val blocklist = service.getIpBlocklistAndUpdateCache()
      logger.info("Blocklist refreshed with ${blocklist.size} ips.")
    } catch (e: Exception) {
      logger.error("Couldn't refresh the blocklist.")
    }
  }
}