package com.eatcloud.authservice.login.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:";

    public RefreshTokenService(RedisTemplate<String, Object> redisTemplate, RestTemplate restTemplate) {
        this.redisTemplate = redisTemplate;
        this.restTemplate = restTemplate;
    }

    private String createKey(String role, UUID id) {
        return "refresh:" + role + ":" + id;
    }

    public Object findUserByRoleAndId(String role, UUID id) {
        String url = switch (role) {
            case "admin" -> "http://admin-service/admin" + id;
            case "manager" -> "http://manager-service/manager/" + id;
            case "customer" -> "http://customer-service/customers/" + id;
            default -> throw new IllegalArgumentException("알 수 없는 역할: " + role);
        };
        return restTemplate.getForObject(url, Object.class);
    }

    public void saveOrUpdateToken(String role, UUID id, String refreshToken, LocalDateTime expiryDateTime) {
        String key = createKey(role, id);
        long duration = Duration.between(LocalDateTime.now(), expiryDateTime).getSeconds();
        redisTemplate.opsForValue().set(key, refreshToken, duration, TimeUnit.SECONDS);
    }

    public boolean isValid(String role, UUID id, String token) {
        String key = createKey(role, id);
        Object stored = redisTemplate.opsForValue().get(key);

        if (redisTemplate.hasKey(BLACKLIST_PREFIX + token)) {
            return false;
        }

        return stored != null && stored.equals(token);
    }

    public void addToBlacklist(String token, long expirationSeconds) {
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "blacklisted", expirationSeconds, TimeUnit.SECONDS);
    }

    /**
     * 토큰 삭제
     */
    public void delete(String role, UUID id) {
        String key = createKey(role, id);
        redisTemplate.delete(key);
    }
}
