package miniredis;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static final int DEFAULT_PORT = 6379;
    public static final int DEFAULT_MAX_ENTRIES = 5;
    public static final String DEFAULT_STORAGE_FILE = "data/cache-store.bin";

    private final int port;
    private final CacheStorage storage;
    private final CacheEventListener eventListener;
    private volatile boolean running;

    public Server(int port, int maxEntries, String storageFile, CacheEventListener eventListener) {
        this.port = port;
        this.eventListener = eventListener;
        this.storage = new CacheStorage(maxEntries, storageFile, eventListener);
    }

    public CacheStorage getStorage() {
        return storage;
    }

    public void start() {
        running = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log("Mini Redis server started on port " + port);
            while (running) {
                Socket clientSocket = serverSocket.accept();
                // Each client gets its own handler thread for concurrent command processing.
                Thread clientThread = new Thread(
                        new ClientHandler(clientSocket, storage, eventListener),
                        "client-" + clientSocket.getPort()
                );
                clientThread.start();
            }
        } catch (IOException e) {
            log("Server error: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
    }

    private void log(String message) {
        if (eventListener != null) {
            eventListener.onEvent(message);
        } else {
            System.out.println(message);
        }
    }

    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        int maxEntries = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_MAX_ENTRIES;
        Server server = new Server(port, maxEntries, DEFAULT_STORAGE_FILE, System.out::println);
        server.start();
    }
}
