package com.example.server;

import com.example.service.Service;
import com.example.common.Credentials;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;

public class Server {

    private int thePort = 0;
    private String theIPAddress = null;
    private ServerSocket serverSocket =  null;

    //Class constructor
    public Server(){
        //Initialize the TCP socket
        thePort = Credentials.PORT;
        theIPAddress = Credentials.HOST;

        //Initialize the socket and runs the service loop
        System.out.println("Server: Initializing server socket at " + theIPAddress + " with listening port " + thePort);
        System.out.println("Server: Exit server application by pressing Ctrl+C (Windows or Linux) or Opt-Cmd-Shift-Esc (Mac OSX)." );
        try {
            //Initialize the socket
            serverSocket = new ServerSocket(thePort, 10, InetAddress.getByName(theIPAddress));
            System.out.println("Server: Server at " + theIPAddress + " is listening on port : " + thePort);
        } catch (Exception e){
            System.out.println(e);
            System.exit(1);
        }
    }

    public void executeServiceLoop()
    {
        System.out.println("Server: Start service loop.");
        try {
            while (true) {
                Socket socket = this.serverSocket.accept();
                new Service(socket);
            }
        } catch (Exception e){
            System.out.println(e);
        }
        System.out.println("Server: Finished service loop.");
    }

    public static void main(String[] args){
        //Run the server
        Server server = new Server();
        server.executeServiceLoop();
        System.out.println("Server: Finished.");
        System.exit(0);
    }
}