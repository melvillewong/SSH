package com.example.client;

import com.example.common.Credentials;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;

public class Client {

    private Socket clientSocket = null;
    private String userCommand = null; // The user command
    private CachedRowSet serviceOutcome = null; // The service outcome

    // Constructor
    public Client() {}

    // Initializes the client socket using the credentials from the Credentials class.
    public void initializeSocket() {
        try {
            System.out.println("Connecting to " + Credentials.HOST + " on port " + Credentials.PORT);
            this.clientSocket = new Socket(Credentials.HOST, Credentials.PORT);
        } catch (UnknownHostException e) {
            System.out.println("Client: Unknown host. " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Client: Unable to connect to server. " + e.getMessage());
        }
    }

    // Sends the user command to the server
    public void requestService() {
        if (this.clientSocket == null) {
            System.out.println("Client: Connection failed. Unable to send request.");
            return;
        }

        try {
            System.out.println("Client: Requesting records database service for user command\n" + this.userCommand);
            OutputStream requestStream = this.clientSocket.getOutputStream();
            OutputStreamWriter requestStreamWriter = new OutputStreamWriter(requestStream);
            requestStreamWriter.write(userCommand + "#");
            requestStreamWriter.flush();
        } catch (IOException e) {
            System.out.println("Client: I/O error while sending request. " + e.getMessage());
        }
    }

    // Processes and prints the server's response
    public void reportServiceOutcome() {
        if (this.clientSocket == null) {
            System.out.println("Client: Connection failed. Unable to process outcome.");
            return;
        }

        try {
            InputStream outcomeStream = clientSocket.getInputStream();
            ObjectInputStream outcomeStreamReader = new ObjectInputStream(outcomeStream);
            serviceOutcome = (CachedRowSet) outcomeStreamReader.readObject();

            if (this.serviceOutcome == null) {
                System.out.println("Client: Received null CachedRowSet from server.");
                return;
            }

            try {
                System.out.println("Client: Processing CachedRowSet...");
                if (!this.serviceOutcome.next()) {
                    System.out.println("Client: No records found in CachedRowSet.");
                    return;
                }

                this.serviceOutcome.beforeFirst();
                while (this.serviceOutcome.next()) {
                    System.out.printf(
                        "Resident ID: %d | Start Time: %s | End Time: %s | Status: %s%n",
                        this.serviceOutcome.getInt("resident_id"),
                        this.serviceOutcome.getTimestamp("start_timestamp"),
                        this.serviceOutcome.getTimestamp("end_timestamp"),
                        this.serviceOutcome.getString("status")
                    );
                }
            } catch (SQLException e) {
                System.out.println("Client: Error processing CachedRowSet: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("Client: I/O error while processing outcome. " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Client: Unable to cast response to CachedRowSet. " + e.getMessage());
        }
    }

    // Executes the client operations
    public void execute() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            // Get user input
            System.out.print("Enter Resident's First Name: ");
            String firstName = reader.readLine();

            System.out.print("Enter Resident's Last Name: ");
            String lastName = reader.readLine();

            // Build user message command
            userCommand = firstName + ";" + lastName;

            // Initialize the socket
            this.initializeSocket();

            // Request service
            this.requestService();

            // Report service outcome
            this.reportServiceOutcome();

            // Close the connection with the server
            if (this.clientSocket != null) {
                this.clientSocket.close();
            }
        } catch (IOException e) {
            System.out.println("Client: Exception occurred during execution. " + e.getMessage());
        }
    }

    // Main method
    public static void main(String[] args) {
        Client client = new Client();
        client.execute();
    }
}