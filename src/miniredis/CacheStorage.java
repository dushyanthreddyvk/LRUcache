package miniredis;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CacheStorage {
    private final Path storageFile;
    private final LRUCache cache;
    private final CacheEventListener eventListener;

    public CacheStorage(int maxEntries, String storagePath, CacheEventListener eventListener) {
        this.storageFile = Path.of(storagePath);
        this.eventListener = eventListener;
        this.cache = new LRUCache(maxEntries, eventListener);
        loadFromDisk();
        startExpirationCleaner();
    }

    public synchronized String set(String key, String value, long ttlSeconds) {
        validateKey(key);
        cache.put(key, new CacheEntry(value, ttlSeconds));
        persist();
        return ttlSeconds > 0
                ? "OK key set with TTL " + ttlSeconds + " seconds"
                : "OK key set";
    }

    public synchronized String get(String key) {
        validateKey(key);
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return "NULL";
        }
        if (entry.isExpired()) {
            removeExpiredKey(key);
            return "NULL";
        }
        return entry.getValue();
    }

    public synchronized String delete(String key) {
        validateKey(key);
        CacheEntry removed = cache.remove(key);
        persist();
        return removed == null ? "KEY_NOT_FOUND" : "DELETED";
    }

    public synchronized Map<String, String> snapshot() {
        // The UI reads a copy so Swing never iterates over the live cache directly.
        cleanupExpiredKeys();
        Map<String, String> visibleCache = new LinkedHashMap<>();
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            CacheEntry cacheEntry = entry.getValue();
            String ttl = cacheEntry.remainingTtlSeconds() >= 0
                    ? " (ttl " + cacheEntry.remainingTtlSeconds() + "s)"
                    : "";
            visibleCache.put(entry.getKey(), cacheEntry.getValue() + ttl);
        }
        return visibleCache;
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key cannot be empty");
        }
    }

    private synchronized void cleanupExpiredKeys() {
        List<String> expiredKeys = new ArrayList<>();
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().isExpired()) {
                expiredKeys.add(entry.getKey());
            }
        }
        for (String key : expiredKeys) {
            removeExpiredKey(key);
        }
    }

    private void removeExpiredKey(String key) {
        cache.remove(key);
        if (eventListener != null) {
            eventListener.onEvent("Key expiration: removed expired key '" + key + "'");
        }
        persist();
    }

    private void startExpirationCleaner() {
        // A daemon thread removes expired keys even when no client is reading them.
        Thread cleaner = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                    synchronized (CacheStorage.this) {
                        cleanupExpiredKeys();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (RuntimeException e) {
                    if (eventListener != null) {
                        eventListener.onEvent("Expiration cleaner error: " + e.getMessage());
                    }
                }
            }
        }, "ttl-cleaner");
        cleaner.setDaemon(true);
        cleaner.start();
    }

    @SuppressWarnings("unchecked")
    private synchronized void loadFromDisk() {
        if (!Files.exists(storageFile)) {
            return;
        }
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(storageFile))) {
            Object saved = input.readObject();
            if (saved instanceof Map<?, ?> savedMap) {
                for (Map.Entry<?, ?> entry : savedMap.entrySet()) {
                    if (entry.getKey() instanceof String && entry.getValue() instanceof CacheEntry cacheEntry) {
                        if (!cacheEntry.isExpired()) {
                            cache.put((String) entry.getKey(), cacheEntry);
                        }
                    }
                }
            }
            if (eventListener != null) {
                eventListener.onEvent("Loaded " + cache.size() + " keys from persistent storage");
            }
        } catch (EOFException e) {
            if (eventListener != null) {
                eventListener.onEvent("Persistent storage is empty");
            }
        } catch (IOException | ClassNotFoundException e) {
            if (eventListener != null) {
                eventListener.onEvent("Could not load persistent storage: " + e.getMessage());
            }
        }
    }

    private synchronized void persist() {
        try {
            // For a small demo cache, full-file persistence keeps the design simple.
            Files.createDirectories(storageFile.getParent());
            try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(storageFile))) {
                output.writeObject(new LinkedHashMap<>(cache));
            }
        } catch (IOException e) {
            if (eventListener != null) {
                eventListener.onEvent("Persistence error: " + e.getMessage());
            }
        }
    }
}
