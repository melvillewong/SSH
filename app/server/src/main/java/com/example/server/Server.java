package com.example.server;

import com.example.service.Service;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private boolean running = false;

    public void startServer() throws IOException {
        serverSocket = new ServerSocket(PORT);
        running = true;
        System.out.println("Server is listening on port " + PORT + "...");

        while (running) {
            try (Socket socket = serverSocket.accept()) {
                System.out.println("New client connected");

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                String clientMessage = in.readLine();
                System.out.println("Received from client: " + clientMessage);

                String response = Service.processMessage(clientMessage);
                out.println(response);
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error handling client: " + e.getMessage());
                } else {
                    System.out.println("Server is shutting down...");
                }
            }
        }
    }

    public void stopServer() throws IOException {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        System.out.println("Server stopped.");
    }

    public static void main(String[] args) {
        Server server = new Server();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stopServer();
            } catch (IOException e) {
                System.err.println("Error during server shutdown: " + e.getMessage());
            }
        }));

        try {
            server.startServer();
        } catch (IOException e) {
            System.err.println("Server failed: " + e.getMessage());
        }
    }
}

 