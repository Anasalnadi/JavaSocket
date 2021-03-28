package com.nado.main;
import com.nado.Server;


public class ServerMain {
    private static final int PORT =9090 ;

    public static void main(String[] args) {
        Server server=new Server(PORT);
        server.start();
    }
}
