package miniredis;

import java.io.Serializable;

class CacheEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String value;
    private final long expiresAtMillis;

    CacheEntry(String value, long ttlSeconds) {
        this.value = value;
        this.expiresAtMillis = ttlSeconds > 0
                ? System.currentTimeMillis() + ttlSeconds * 1000
                : 0;
    }

    String getValue() {
        return value;
    }

    long getExpiresAtMillis() {
        return expiresAtMillis;
    }

    boolean isExpired() {
        return expiresAtMillis > 0 && System.currentTimeMillis() >= expiresAtMillis;
    }

    long remainingTtlSeconds() {
        if (expiresAtMillis == 0) {
            return -1;
        }
        return Math.max(0, (expiresAtMillis - System.currentTimeMillis()) / 1000);
    }
}
