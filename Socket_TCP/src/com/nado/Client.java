package com.nado;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 9090;

//    private static final String SERVER_IP ="localhost" ;
//    private static final int SERVER_PORT = 9090;
//
//    private Socket socket;
//
//    public Client(String serverIP){
//
//            if(!connectServer(serverIP)) {
//                System.out.println("Failed to open socket connection to: " + serverIP);
//            }
//    }
//
//
//    public boolean connectServer(String serverIp){
//        try {
//            socket=new Socket(serverIp,SERVER_PORT);
//
//            System.out.println("00. -> Connected to Server:" + this.socket.getInetAddress()
//                    + " on port: " + this.socket.getPort());
//
//            System.out.println("    -> from local address: " + this.socket.getLocalAddress()
//                    + " and port: " + this.socket.getLocalPort());
//
//        } catch (IOException e) {
//            System.out.println("XX. Failed to Connect to the Server at port: " + SERVER_PORT);
//            System.out.println("    Exception: " + e.toString());
//            return false;
//
//        }
//
//        return true;
//    }




    public static void main(String[] args) throws IOException {

        //Client client = new Client(SERVER_IP);

        Socket socket =new Socket(SERVER_IP,SERVER_PORT);

        BufferedReader reader = new BufferedReader(new InputStreamReader( socket.getInputStream() ));

        String serverResponse = reader.readLine();

        //display
        JOptionPane.showMessageDialog(null,serverResponse);
        //System.out.println(serverResponse);

        socket.close();
        System.exit(0);

    }


}
