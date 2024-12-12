package com.example.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

// import java.io.IOException;
// import java.net.Socket;

public class ServerTest {
    private Server server;

    @BeforeEach
    void setUp() {
        server = new Server();
    }

    @Test
    @Timeout(5) // This test should complete within 5 seconds
    void testServerInitialization() {
        assertNotNull(server);
    }

    // @Test
    // void testServerListening() throws IOException {
    //     // Start the server in a separate thread
    //     Thread serverThread = new Thread(() -> server.executeServiceLoop());
    //     serverThread.start();

    //     // Give the server a moment to start
    //     try {
    //         Thread.sleep(1000);
    //     } catch (InterruptedException e) {
    //         e.printStackTrace();
    //     }

    //     // Try to connect to the server
    //     try (Socket socket = new Socket("localhost", com.example.common.Credentials.PORT)) {
    //         assertTrue(socket.isConnected());
    //     }

    //     // Stop the server thread
    //     serverThread.interrupt();
    // }
}
