package miniredis;

public interface CacheEventListener {
    void onEvent(String message);
}
