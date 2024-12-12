package com.example.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    private Client client;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        client = new Client();
        System.setOut(new PrintStream(outContent));
    }

    @Test
    @Timeout(5)
    void testClientExecution() {
        String input = "John\nDoe\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        client.execute();

        String output = outContent.toString();
        assertTrue(output.contains("Enter Resident's First Name:"));
        assertTrue(output.contains("Enter Resident's Last Name:"));
        assertTrue(output.contains("Connecting to"));
    }

    @Test
    void testInitializeSocket() {
        client.initializeSocket();
        String output = outContent.toString();
        assertTrue(output.contains("Connecting to"));
    }
}
