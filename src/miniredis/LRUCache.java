package miniredis;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache extends LinkedHashMap<String, CacheEntry> {
    private static final long serialVersionUID = 1L;

    private final int maxEntries;
    private transient CacheEventListener eventListener;

    public LRUCache(int maxEntries, CacheEventListener eventListener) {
        // accessOrder=true makes reads update recency, which is the core of LRU behavior.
        super(16, 0.75f, true);
        this.maxEntries = maxEntries;
        this.eventListener = eventListener;
    }

    void setEventListener(CacheEventListener eventListener) {
        this.eventListener = eventListener;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
        // LinkedHashMap calls this after each insert, giving O(1) eviction.
        boolean shouldEvict = size() > maxEntries;
        if (shouldEvict && eventListener != null) {
            eventListener.onEvent("Cache eviction: removed least recently used key '" + eldest.getKey() + "'");
        }
        return shouldEvict;
    }
}
