package miniredis;

public class CommandProcessor {
    private final CacheStorage storage;

    public CommandProcessor(CacheStorage storage) {
        this.storage = storage;
    }

    public String process(String commandLine) {
        if (commandLine == null || commandLine.isBlank()) {
            return "ERROR empty command";
        }

        String[] parts = commandLine.trim().split("\\s+", 4);
        String command = parts[0].toUpperCase();

        try {
            return switch (command) {
                case "SET" -> handleSet(parts);
                case "GET" -> handleGet(parts);
                case "DELETE" -> handleDelete(parts);
                case "EXIT" -> "GOODBYE";
                default -> "ERROR unknown command";
            };
        } catch (IllegalArgumentException e) {
            return "ERROR " + e.getMessage();
        } catch (RuntimeException e) {
            return "ERROR internal server error: " + e.getMessage();
        }
    }

    private String handleSet(String[] parts) {
        if (parts.length < 3) {
            throw new IllegalArgumentException("SET requires: SET key value [ttlSeconds]");
        }

        String key = parts[1];
        String value = parts[2];
        long ttlSeconds = 0;

        if (parts.length == 4) {
            String[] valueAndTtl = parts[3].split("\\s+");
            if (valueAndTtl.length > 1) {
                throw new IllegalArgumentException("Value cannot contain spaces when TTL is provided");
            }
            ttlSeconds = parseTtl(parts[3]);
        }

        return storage.set(key, value, ttlSeconds);
    }

    private String handleGet(String[] parts) {
        if (parts.length != 2) {
            throw new IllegalArgumentException("GET requires: GET key");
        }
        return storage.get(parts[1]);
    }

    private String handleDelete(String[] parts) {
        if (parts.length != 2) {
            throw new IllegalArgumentException("DELETE requires: DELETE key");
        }
        return storage.delete(parts[1]);
    }

    private long parseTtl(String ttl) {
        try {
            long ttlSeconds = Long.parseLong(ttl);
            if (ttlSeconds < 0) {
                throw new IllegalArgumentException("TTL cannot be negative");
            }
            return ttlSeconds;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("TTL must be a number of seconds");
        }
    }
}
