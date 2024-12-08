/*
 * RecordsDatabaseService.java
 *
 * The service threads for the records database server.
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

    //Class constructor
    public RecordsDatabaseService(Socket aSocket){
        
		//TO BE COMPLETED
        serviceSocket = aSocket;
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
            System.out.println("Service thread " + this.getId() + ": " + e);
        }
        return this.requestStr;
    }

    //Parse the request command and execute the query
    public boolean attendRequest()
    {
        boolean flagRequestAttended = true;
		
		this.outcome = null;
		
		String sql = "SELECT record.title,record.label,record.genre,record.rrp,COUNT(recordcopy.copyID) AS copies FROM record JOIN artist ON record.artistID = artist.artistID JOIN recordcopy ON record.recordID = recordcopy.recordID JOIN recordshop ON recordcopy.recordshopID = recordshop.recordshopID where artist.lastname = ? AND recordshop.city = ? GROUP BY record.title,record.label,record.genre,record.rrp HAVING COUNT(recordcopy.copyID)>0"; //TO BE COMPLETED- Update this line as needed.

        try (Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement stmt = con.prepareStatement(sql)) {

            System.out.println("Executing query with parameters: artist=" + requestStr[0] + ", recordshop=" + requestStr[1]);
            stmt.setString(1, requestStr[0]);
            stmt.setString(2, requestStr[1]);

            ResultSet rs = stmt.executeQuery();
            RowSetFactory rowSetFactory = RowSetProvider.newFactory();
            CachedRowSet crs = rowSetFactory.createCachedRowSet();
            crs.populate(rs);
            this.outcome = crs;

            if (!crs.next()) {
                System.out.println("No data found in CachedRowSet after population.");
            } else {
                System.out.println("Data found in CachedRowSet:");
                crs.beforeFirst(); // Reset cursor
                while (crs.next()) {
                    System.out.println(
                            "Title: " + crs.getString("title") +
                                    " | Label: " + crs.getString("label") +
                                    " | Genre: " + crs.getString("genre") +
                                    " | RRP: " + crs.getDouble("rrp") +
                                    " | Copies: " + crs.getInt("copies")
                    );
                }
                crs.beforeFirst(); // Reset for client
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        return flagRequestAttended;
    }

    //Wrap and return service outcome
    public void returnServiceOutcome(){
        try {
			//Return outcome
			//TO BE COMPLETED
            OutputStream outcomeStream = this.serviceSocket.getOutputStream();
            ObjectOutputStream outcomeStreamWriter = new ObjectOutputStream(outcomeStream);
            outcomeStreamWriter.writeObject(this.outcome);
            outcomeStreamWriter.flush();
            outcomeStreamWriter.close();
            if (this.outcome == null) {
                System.out.println("Service thread " + this.getId() + ": No data to send.");
            } else {
                System.out.println("Service thread " + this.getId() + ": Sending CachedRowSet with data:");
                try {
                    while (this.outcome.next()) {
                        System.out.println(
                                "Title: " + this.outcome.getString("title") +
                                        " | Label: " + this.outcome.getString("label") +
                                        " | Genre: " + this.outcome.getString("genre") +
                                        " | RRP: " + this.outcome.getDouble("rrp") +
                                        " | Copies: " + this.outcome.getInt("copies")
                        );
                    }
                    this.outcome.beforeFirst(); // Reset the cursor for the client to iterate over it
                } catch (SQLException e) {
                    System.out.println("Error iterating over CachedRowSet: " + e.getMessage());
                }
            }
            while (outcome.next()) {
                String title = outcome.getString("title");
                String label = outcome.getString("label");
                String genre = outcome.getString("genre");
                double price = outcome.getDouble("rrp");
                int copies = outcome.getInt("copies");

                System.out.println(title + " | " + label + " | " + genre + " | " + price + " | " + copies);
            }
            System.out.println("Service thread " + this.getId() + ": Service outcome returned; " + this.outcome);
            
			//Terminating connection of the service socket
			//TO BE COMPLETED
            this.serviceSocket.close();
        }catch (IOException e){
            System.out.println("Service thread " + this.getId() + ": " + e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //The service thread run() method
    public void run()
    {
		try {
			System.out.println("\n============================================\n");
            //Retrieve the service request from the socket
            this.retrieveRequest();
            System.out.println("Service thread " + this.getId() + ": Request retrieved: "
						+ "artist->" + this.requestStr[0] + "; recordshop->" + this.requestStr[1]);

            //Attend the request
            boolean tmp = this.attendRequest();

            //Send back the outcome of the request
            if (!tmp)
                System.out.println("Service thread " + this.getId() + ": Unable to provide service.");
            this.returnServiceOutcome();

        }catch (Exception e){
            System.out.println("Service thread " + this.getId() + ": " + e);
        }
        //Terminate service thread (by exiting run() method)
        System.out.println("Service thread " + this.getId() + ": Finished service.");
    }

}
