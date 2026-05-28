package miniredis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConsoleClient {
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : Server.DEFAULT_PORT;

        try (
                Socket socket = new Socket(host, port);
                BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedReader keyboardReader = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            System.out.println(serverReader.readLine());
            String command;
            while ((command = keyboardReader.readLine()) != null) {
                writer.println(command);
                String response = serverReader.readLine();
                System.out.println(response);
                if ("EXIT".equalsIgnoreCase(command.trim())) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}
