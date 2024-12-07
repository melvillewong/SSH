package com.example.service;

public class Service {
    public static String processMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "Error: Message cannot be empty.";
        }
        // Example: Reverse the client's message
        return "Service processed message: " + new StringBuilder(message).reverse();
    }
}
