package com.github.fernandospr.blocklist.config

import com.github.fernandospr.blocklist.BLOCKLIST_NODE_TYPE_CLUSTERMEMBER
import com.github.fernandospr.blocklist.BLOCKLIST_NODE_TYPE_CLUSTERREFRESHER
import com.github.fernandospr.blocklist.BLOCKLIST_NODE_TYPE_STANDALONE
import com.hazelcast.config.Config
import com.hazelcast.config.NearCacheConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.spring.cache.HazelcastCacheManager
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnExpression(
  "'\${blocklist.nodeType:$BLOCKLIST_NODE_TYPE_STANDALONE}'.equals('$BLOCKLIST_NODE_TYPE_CLUSTERREFRESHER') or " +
      "'\${blocklist.nodeType:$BLOCKLIST_NODE_TYPE_STANDALONE}'.equals('$BLOCKLIST_NODE_TYPE_CLUSTERMEMBER')"
)
class CacheConfiguration {
  @Bean
  fun hazelcastInstance(): HazelcastInstance {
    val config = Config()
    config.getMapConfig("blocklist").nearCacheConfig = NearCacheConfig()
    return Hazelcast.newHazelcastInstance(config)
  }

  @Bean
  fun cacheManager(hazelcastInstance: HazelcastInstance): CacheManager {
    return HazelcastCacheManager(hazelcastInstance)
  }
}