package com.example.service;

import com.example.common.Credentials;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import javax.sql.rowset.*;

public class Service extends Thread{

    private Socket serviceSocket = null;
    private String[] requestStr  = new String[2]; //One slot for first name and one for last name.
    private ResultSet outcome   = null;

	//JDBC connection
    private String USERNAME = Credentials.USERNAME;
    private String PASSWORD = Credentials.PASSWORD;
    private String URL      = Credentials.URL;

    private void runSqlScript(String scriptName) {
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            System.out.println("Executing SQL script: " + scriptName);
            
            // Load the SQL script from the resources folder
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(scriptName)) {
                if (inputStream == null) {
                    throw new FileNotFoundException("Script not found: " + scriptName);
                }
                
                // Read the script from the InputStream
                StringBuilder sqlBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sqlBuilder.append(line).append("\n");
                    }
                } 
                
                // Split the script into individual SQL commands
                String[] sqlCommands = sqlBuilder.toString().split(";");
                try (Statement stmt = connection.createStatement()) {
                    for (String sql : sqlCommands) {
                        if (!sql.trim().isEmpty()) {
                            stmt.execute(sql.trim());
                        }
                    }
                }
                
                System.out.println("SQL script executed successfully.");
            } catch (IOException | SQLException e) {
                System.err.println("Error executing SQL script: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Class constructor
    public Service(Socket aSocket){
        serviceSocket = aSocket;
        runSqlScript("autorun_chore_hour_suggestion.sql");
        runSqlScript("autorun_total_hour_suggestion.sql");
        System.out.println("SQL script executed and database state verified.");
        this.start();
    }

    //Retrieve the request from the socket
    public String[] retrieveRequest()
    {
        this.requestStr[0] = ""; //For first name
        this.requestStr[1] = ""; //For last name
		
        try {

            InputStream socketStream = this.serviceSocket.getInputStream();
            InputStreamReader socketReader = new InputStreamReader(socketStream);
            StringBuffer firstNameStringBuffer = new StringBuffer();
            StringBuffer lastNameStringBuffer = new StringBuffer();
            boolean deter = true;
            char x;
            while (true)
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
            ),
            solo_time AS (
                SELECT 
                    ts.resident_id, 
                    ts.start_timestamp, 
                    ts.end_timestamp, 
                    CASE 
                        WHEN ts.resident_id = 0 THEN 'Empty'
                        ELSE 'Solo'
                    END AS status,
                    NULL::TEXT AS chore_type -- Placeholder for union compatibility
                FROM total_hour_suggestions ts
                WHERE ts.resident_id = 0 
                OR ts.resident_id = (SELECT resident_id FROM selected_resident)
            ),
            chore_suggestions AS (
                SELECT 
                    ts.resident_id, 
                    ts.start_timestamp, 
                    ts.end_timestamp, 
                    'Chore' AS status, -- Label to distinguish chore suggestions
                    ts.chore_type
                FROM chore_hours_suggestions ts
                WHERE ts.resident_id = (SELECT resident_id FROM selected_resident)
            )
            SELECT *
            FROM solo_time
            UNION ALL
            SELECT *
            FROM chore_suggestions
            ORDER BY resident_id, start_timestamp;
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
                outcomeStreamWriter.writeObject(null); 
            } else {
                outcomeStreamWriter.writeObject(this.outcome); 
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
