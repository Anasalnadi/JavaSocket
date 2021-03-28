package com.nado;

import com.nado.thread.ServerHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private final int serverPort;
    private List<ServerHandler> serverListClient=new ArrayList<>();

     public Server(int serverPort) {
        this.serverPort=serverPort;
    }

    public List<ServerHandler> getClientList(){
         return serverListClient;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket=new ServerSocket(serverPort);
            while (true){
                System.out.println("wait to accept client Connection........");
                Socket clientSocket=serverSocket.accept();
                System.out.println("Accepted Connection from "+ clientSocket);

                ServerHandler newClient =new ServerHandler(this,clientSocket);
                serverListClient.add(newClient);
                //System.out.println(getClientList().toString());
                newClient.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeClientfromServer(ServerHandler serverHandler) {
         serverListClient.remove(serverHandler);
    }
}
