package ru.prod.buysell;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerificationCodeService {

    private static final int CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();
    private static final String CACHE_NAME = "verificationCodes";

    private final CacheManager cacheManager;

    public String generateCode() {
        int code = 100_000 + random.nextInt(900_000);
        return String.valueOf(code);
    }

    public void storeCode(String email, String code) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.put(email, code);
            log.info("Сохранён код верификации для email: {}", email);
        } else {
            log.error("Cache {} not found", CACHE_NAME);
        }
    }

    public String getCode(String email) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            return cache.get(email, String.class);
        }
        return null;
    }

    public void deleteCode(String email) {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null) {
            cache.evict(email);
            log.info("Удалён код верификации для email: {}", email);
        }
    }

    public boolean validateCode(String email, String providedCode) {
        String storedCode = getCode(email);
        if (storedCode == null) {
            log.warn("Не найден код верификации для email: {}", email);
            return false;
        }
        boolean valid = storedCode.equals(providedCode);
        if (valid) {
            deleteCode(email);
        }
        return valid;
    }
}