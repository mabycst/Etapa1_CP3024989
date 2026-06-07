package com.example.userservice.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CodigoCacheService {

    private static class CacheEntry {
        private final String code;
        private final LocalDateTime createdAt;

        public CacheEntry(String code) {
            this.code = code;
            this.createdAt = LocalDateTime.now();
        }

        public String getCode() {
            return code;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(createdAt.plusMinutes(5));
        }
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public void save(String email, String code) {
        cache.put(email, new CacheEntry(code));
    }

    public String get(String email) {
        CacheEntry entry = cache.get(email);
        if (entry != null) {
            if (entry.isExpired()) {
                cache.remove(email);
                return null;
            }
            return entry.getCode();
        }
        return null;
    }

    public void delete(String email) {
        cache.remove(email);
    }

    // Limpa registros expirados a cada minuto para liberar memória
    @Scheduled(fixedRate = 60000)
    public void cleanExpiredEntries() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
