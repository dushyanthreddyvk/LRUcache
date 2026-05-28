package miniredis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final CommandProcessor commandProcessor;
    private final CacheEventListener eventListener;

    public ClientHandler(Socket clientSocket, CacheStorage storage, CacheEventListener eventListener) {
        this.clientSocket = clientSocket;
        this.commandProcessor = new CommandProcessor(storage);
        this.eventListener = eventListener;
    }

    @Override
    public void run() {
        String clientName = clientSocket.getRemoteSocketAddress().toString();
        if (eventListener != null) {
            eventListener.onEvent("Client connected: " + clientName);
        }

        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            writer.println("Connected to Mini Redis. Commands: SET key value [ttl], GET key, DELETE key, EXIT");

            String line;
            while ((line = reader.readLine()) != null) {
                String response = commandProcessor.process(line);
                writer.println(response);
                if ("EXIT".equalsIgnoreCase(line.trim()) || "GOODBYE".equals(response)) {
                    break;
                }
            }
        } catch (IOException e) {
            if (eventListener != null) {
                eventListener.onEvent("Client error " + clientName + ": " + e.getMessage());
            }
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                if (eventListener != null) {
                    eventListener.onEvent("Socket close error: " + e.getMessage());
                }
            }
            if (eventListener != null) {
                eventListener.onEvent("Client disconnected: " + clientName);
            }
        }
    }
}
