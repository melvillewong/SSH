package com.example.service;

import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.net.Socket;
import java.sql.*;

import javax.sql.rowset.CachedRowSet;
// import javax.sql.rowset.RowSetProvider;

// import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ServiceTest {

    @Mock
    private Socket mockSocket;
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private CachedRowSet mockCachedRowSet;

    private Service service;

    @BeforeEach
    void setUp() throws SQLException, IOException {
        MockitoAnnotations.openMocks(this);
        service = new Service(mockSocket);

        // Mock the input stream to simulate incoming request data
        ByteArrayInputStream inputStream = new ByteArrayInputStream("John;Doe#".getBytes());
        when(mockSocket.getInputStream()).thenReturn(inputStream);

        // Call retrieveRequest to set the requestStr
        service.retrieveRequest();

        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    }

    // @Test
    // void testRetrieveRequest() throws IOException {
    //     // Prepare mock input stream
    //     String input = "John;Doe#";
    //     InputStream inputStream = new ByteArrayInputStream(input.getBytes());
    //     when(mockSocket.getInputStream()).thenReturn(inputStream);

    //     // Call method to retrieve request
    //     String[] request = service.retrieveRequest();

    //     // Verify results
    //     assertEquals("John", request[0]);
    //     assertEquals("Doe", request[1]);
    // }

    // @Test
    // void testAttendRequest() throws SQLException {
    //     when(mockResultSet.next()).thenReturn(true, false);
    //     when(RowSetProvider.newFactory().createCachedRowSet()).thenReturn(mockCachedRowSet);

    //     boolean result = service.attendRequest();

    //     assertTrue(result);
    //     verify(mockPreparedStatement).setString(1, "John");
    //     verify(mockPreparedStatement).setString(2, "Doe");
    //     verify(mockCachedRowSet).populate(mockResultSet);
    // }

    // @Test
    // void testReturnServiceOutcome() throws SQLException, IOException {
    //     // Set up mock behavior
    //     when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
    //     when(DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(mockConnection);
    //     when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
    //     when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
    //     when(RowSetProvider.newFactory().createCachedRowSet()).thenReturn(mockCachedRowSet);
    //     when(mockCachedRowSet.next()).thenReturn(true, false);

    //     // Simulate a successful request
    //     service.retrieveRequest();
    //     service.attendRequest();

    //     // Call the method under test
    //     service.returnServiceOutcome();

    //     // Verify the expected behavior
    //     verify(mockSocket).getOutputStream();
    //     verify(mockSocket).close();
    //     verify(mockCachedRowSet).next();
    // }

    // @Test
    // void testRun() throws SQLException, IOException {
    //     when(mockResultSet.next()).thenReturn(true, false);
    //     when(RowSetProvider.newFactory().createCachedRowSet()).thenReturn(mockCachedRowSet);
    //     when(mockCachedRowSet.next()).thenReturn(true);

    //     service.run();

    //     verify(mockSocket).close();
    // }
}