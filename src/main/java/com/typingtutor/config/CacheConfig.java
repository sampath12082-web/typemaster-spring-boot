package com.typingtutor.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
            buildCache("leaderboard",   60,  1_000),  // 60s TTL — updates as users complete lessons
            buildCache("placement",   3600,     10)   // 1h TTL  — static passage, rarely changes
        ));
        return manager;
    }

    private CaffeineCache buildCache(String name, long ttlSeconds, int maxSize) {
        return new CaffeineCache(name, Caffeine.newBuilder()
            .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
            .maximumSize(maxSize)
            .build());
    }
}
