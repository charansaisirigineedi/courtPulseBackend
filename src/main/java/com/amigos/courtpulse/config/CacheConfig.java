package com.amigos.courtpulse.config;

import com.amigos.courtpulse.util.CacheNames;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                buildCache(CacheNames.PLAYER_PROFILES, Duration.ofMinutes(30), 500),
                buildCache(CacheNames.PLAYER_ANALYTICS_LIFETIME, Duration.ofMinutes(15), 1_000),
                buildCache(CacheNames.PLAYER_ANALYTICS_TOURNAMENTS, Duration.ofMinutes(15), 1_000),
                buildCache(CacheNames.PLAYER_ANALYTICS_CLUB, Duration.ofMinutes(15), 2_000),
                buildCache(CacheNames.CLUB_PROFILES, Duration.ofMinutes(30), 500),
                buildCache(CacheNames.CLUB_MEMBERS, Duration.ofMinutes(15), 500),
                buildCache(CacheNames.CLUB_TOURNAMENTS, Duration.ofMinutes(15), 500),
                buildCache(CacheNames.CLUB_RECENT_COMPLETED_TOURNAMENTS, Duration.ofMinutes(15), 500),
                buildCache(CacheNames.TOURNAMENT_DETAILS, Duration.ofMinutes(30), 500),
                buildCache(CacheNames.TOURNAMENT_SUMMARIES, Duration.ofMinutes(15), 500),
                buildCache(CacheNames.PLAYER_SEARCH, Duration.ofSeconds(60), 200),
                buildCache(CacheNames.CLUB_SEARCH, Duration.ofSeconds(60), 200)
        ));
        return cacheManager;
    }

    private CaffeineCache buildCache(String name, Duration ttl, int maxSize) {
        return new CaffeineCache(
                name,
                Caffeine.newBuilder()
                        .expireAfterWrite(ttl)
                        .maximumSize(maxSize)
                        .recordStats()
                        .build()
        );
    }
}
