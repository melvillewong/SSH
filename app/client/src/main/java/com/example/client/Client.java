package com.example.client;

import java.io.*;
import java.net.Socket;

public class Client implements AutoCloseable {
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;

    // Constructor for testing or custom Socket
    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // Default constructor for actual use
    public Client(String host, int port) throws IOException {
        this(new Socket(host, port));
    }

    public String sendMessage(String message) throws IOException {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        out.println(message);
        return in.readLine();
    }

    @Override
    public void close() throws IOException {
        try {
            in.close();
            out.close();
        } finally {
            socket.close();
        }
    }

    public static void main(String[] args) {
        try (Client client = new Client("localhost", 12345)) {
            String response = client.sendMessage("Hello, Server!");
            System.out.println("Response from server: " + response);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
