/*
 * RecordsDatabaseService.java
 *
 * The service threads for the ssh smart scheduling database server.
 * This class implements the database access service, i.e. opens a JDBC connection
 * to the database, makes and retrieves the query, and sends back the result.
 *
 */

import java.io.*;
import java.net.Socket;
import java.sql.*;
import javax.sql.rowset.*;

public class RecordsDatabaseService extends Thread{

    private Socket serviceSocket = null;
    private String[] requestStr  = new String[2]; //One slot for first name and one for last name.
    private ResultSet outcome   = null;

	//JDBC connection
    private String USERNAME = Credentials.USERNAME;
    private String PASSWORD = Credentials.PASSWORD;
    private String URL      = Credentials.URL;

    private void runSqlScript(String filePath) {
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            System.out.println("Executing SQL script: " + filePath);
            
            // Read and execute SQL commands from the file
            StringBuilder sqlBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sqlBuilder.append(line).append("\n");
                }
            }
            
            // Split commands by semicolon and execute them
            String[] sqlCommands = sqlBuilder.toString().split(";");
            try (Statement stmt = connection.createStatement()) {
                for (String sql : sqlCommands) {
                    if (!sql.trim().isEmpty()) {
                        stmt.execute(sql.trim());
                    }
                }
            }
            
            System.out.println("SQL script executed successfully.");
        } catch (SQLException | IOException e) {
            System.err.println("Error executing SQL script: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Class constructor
    public RecordsDatabaseService(Socket aSocket){
        serviceSocket = aSocket;
        runSqlScript("autorun_chore_hour_suggestion.sql");
        System.out.println("SQL script executed and database state verified.");
        this.start();
    }

    //Retrieve the request from the socket
    public String[] retrieveRequest()
    {
        this.requestStr[0] = ""; //For first name
        this.requestStr[1] = ""; //For last name
		
        try {

			//TO BE COMPLETED
            InputStream socketStream = this.serviceSocket.getInputStream();
            InputStreamReader socketReader = new InputStreamReader(socketStream);
            StringBuffer firstNameStringBuffer = new StringBuffer();
            StringBuffer lastNameStringBuffer = new StringBuffer();
            boolean deter = true;
            char x;
            while (true) //Read until terminator character is found
            {
                x = (char) socketReader.read();
                if (x == '#') {
                    break;
                } else if (x == ';') {
                    deter = false;
                    continue;
                }
                if (deter) {
                    firstNameStringBuffer.append(x);
                } else {
                    lastNameStringBuffer.append(x);
                }
            }
            this.requestStr[0] = firstNameStringBuffer.toString();
            this.requestStr[1]= lastNameStringBuffer.toString();
			
         }catch(IOException e){
            System.out.println("Service thread " + this.getName() + ": " + e);
        }
        return this.requestStr;
    }

    //Parse the request command and execute the query
    public boolean attendRequest()
    {
        boolean flagRequestAttended = true;
		
		this.outcome = null;
		
		String sql = """
            WITH selected_resident AS (
                SELECT resident_id
                FROM residents
                WHERE firstName = ? AND lastName = ?
            )
            SELECT 
                ts.resident_id, 
                ts.chore_type,
                ts.start_timestamp, 
                ts.end_timestamp, 
            FROM chore_hour_suggestions ts
            WHERE ts.resident_id = (SELECT resident_id FROM selected_resident)
            ORDER BY ts.resident_id;
            """;

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            PreparedStatement stmt = con.prepareStatement(sql)) {

            System.out.println("Executing query with parameters: firstName=" + requestStr[0] + ", lastName=" + requestStr[1]);
            stmt.setString(1, requestStr[0]);
            stmt.setString(2, requestStr[1]);

            ResultSet rs = stmt.executeQuery();
            RowSetFactory rowSetFactory = RowSetProvider.newFactory();
            CachedRowSet crs = rowSetFactory.createCachedRowSet();
            crs.populate(rs);
            this.outcome = crs;
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        return flagRequestAttended;
    }

    //Wrap and return service outcome
    public void returnServiceOutcome() {
        try {
            // Create an output stream to send the outcome back to the client
            OutputStream outcomeStream = this.serviceSocket.getOutputStream();
            ObjectOutputStream outcomeStreamWriter = new ObjectOutputStream(outcomeStream);
    
            // Check if there is data to send
            if (this.outcome == null || !this.outcome.next()) {
                System.out.println("Service thread " + this.getName() + ": No data to send.");
                outcomeStreamWriter.writeObject(null); // Send a null object if no data
            } else {
                System.out.println("Service thread " + this.getName() + ": Sending CachedRowSet with data:");
                this.outcome.beforeFirst(); // Reset the cursor for sending data
                while (this.outcome.next()) {
                    System.out.println(
                            "Resident ID: " + this.outcome.getInt("resident_id") +
                            " | Start Time: " + this.outcome.getTimestamp("start_timestamp") +
                            " | End Time: " + this.outcome.getTimestamp("end_timestamp") +
                            " | Status: " + this.outcome.getString("status")
                    );
                }
                this.outcome.beforeFirst(); // Reset the cursor for the client to process
                outcomeStreamWriter.writeObject(this.outcome); // Send the CachedRowSet object
            }
    
            // Flush and close the stream
            outcomeStreamWriter.flush();
            outcomeStreamWriter.close();
    
            // Close the service socket
            this.serviceSocket.close();
            System.out.println("Service thread " + this.getName() + ": Service outcome returned and connection closed.");
        } catch (IOException | SQLException e) {
            System.err.println("Service thread " + this.getName() + ": Error while returning outcome: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //The service thread run() method
    public void run() {
        System.out.println("\n============================================\n");
    
        try {
            // Retrieve the service request from the socket
            this.retrieveRequest();
            System.out.println("Service thread " + this.getName() + ": Request retrieved: "
                    + "firstName->" + this.requestStr[0] + "; lastName->" + this.requestStr[1]);
    
            // Attend the request and execute the database query
            boolean isRequestSuccessful = this.attendRequest();
    
            if (isRequestSuccessful) {
                this.returnServiceOutcome();
            } else {
                System.out.println("Service thread " + this.getName() + ": Unable to process the request successfully.");
            }
    
        } catch (Exception e) {
            System.err.println("Service thread " + this.getName() + ": Unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (this.serviceSocket != null && !this.serviceSocket.isClosed()) {
                    this.serviceSocket.close();
                    System.out.println("Service thread " + this.getName() + ": Connection closed.");
                }
            } catch (IOException e) {
                System.err.println("Service thread " + this.getName() + ": Error while closing socket: " + e.getMessage());
            }
        }
    
        System.out.println("Service thread " + this.getName() + ": Finished service.");
    }

}
