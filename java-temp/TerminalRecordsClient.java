import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;

public class TerminalRecordsClient {

    private Socket clientSocket = null;
    private String userCommand = null; // The user command
    private CachedRowSet serviceOutcome = null; // The service outcome

    // Constructor
    public TerminalRecordsClient() {}

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

                StringBuilder statusRecords = new StringBuilder("== Status Records ==\n");
                StringBuilder choreRecords = new StringBuilder("== Chore Suggestions ==\n");
                boolean hasStatusRecords = false;
                boolean hasChoreRecords = false;

                this.serviceOutcome.beforeFirst();
                while (this.serviceOutcome.next()) {
                    // Check the status to differentiate between solo time and chore suggestions
                    String status = this.serviceOutcome.getString("status");
                    if ("Solo".equals(status) || "Empty".equals(status)) {
                        // Add to status records
                        statusRecords.append(String.format(
                            "Resident ID: %d | Status: %s | Start Time: %s | End Time: %s%n",
                            this.serviceOutcome.getInt("resident_id"),
                            status,
                            this.serviceOutcome.getTimestamp("start_timestamp"),
                            this.serviceOutcome.getTimestamp("end_timestamp")
                        ));
                        hasStatusRecords = true;
                    } else {
                        // Add to chore suggestion records
                        choreRecords.append(String.format(
                            "Resident ID: %d | Chore Type: %s | Start Time: %s | End Time: %s%n",
                            this.serviceOutcome.getInt("resident_id"),
                            this.serviceOutcome.getString("chore_type"),
                            this.serviceOutcome.getTimestamp("start_timestamp"),
                            this.serviceOutcome.getTimestamp("end_timestamp")
                        ));
                        hasChoreRecords = true;
                    }
                }
                if (hasStatusRecords) {
                    System.out.println(statusRecords);
                } else {
                    System.out.println("No status records found.");
                }
    
                if (hasChoreRecords) {
                    System.out.println(choreRecords);
                } else {
                    System.out.println("No chore suggestion records found.");
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
        TerminalRecordsClient client = new TerminalRecordsClient();
        client.execute();
    }
}
