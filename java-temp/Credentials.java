public class Credentials {
    //JDBC connection
    public static final String USERNAME = "postgres";
    public static final String PASSWORD = "011006";
    public static final String URL = "jdbc:postgresql://localhost:5432/ssh_smart_scheduling";
    //Client-server connection
    public static final String HOST = "127.0.0.1"; //localhost
    public static final int PORT = 9994; //This is NOT the port in postgres, but the port at which the RecordsDatabaseServer is listening
}
