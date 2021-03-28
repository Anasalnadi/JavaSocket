package com.nado.thread;

import com.nado.Server;
import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

public class ServerHandler extends Thread{
    private final Socket clientSocket;
    private final Server server;
    private String loginUser;
    private OutputStream output;
    private final HashSet<String>topicSet=new HashSet<>();

    public ServerHandler(Server server, Socket clientSocket) {
        this.server=server;
        this.clientSocket=clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public  String getLoginUser(){
        return loginUser;
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        this.output = clientSocket.getOutputStream();
        InputStream input = clientSocket.getInputStream();
        BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(input));
        String line;

        while ((line=bufferedReader.readLine())!=null){
            // used lib to help split token
             String[] tokens= StringUtils.split(line);
             if (tokens != null && tokens.length>0){
                 String cmd=tokens[0];
                 // Logoff
                    if("logoff".equals(cmd)||"exit".equalsIgnoreCase(cmd)){
                        handelLogoff();
                        break;
                    }// Login
                    else if("login".equalsIgnoreCase(cmd)){
                        hadleLogin(output,tokens);
                    }// Message
                    else if("msg".equalsIgnoreCase(cmd)){
                        String[]tokenMessage=StringUtils.split(line,null,3);
                        handelMessage(tokenMessage);
                    }else if ("join".equalsIgnoreCase(cmd)){
                        hadleJoin(tokens);
                    }else if ("leave".equalsIgnoreCase(cmd)){
                        hadelLeave(tokens);
                    }else{
                        String msg="[Server] Unknown : "+cmd+"\n";
                        output.write(msg.getBytes());
                    }
                }
            }
        clientSocket.close();
    }

    private void hadelLeave(String[] tokens) {
        if (tokens.length > 1){
            String topic=tokens[1];
            topicSet.remove(topic);
        }
    }

    public boolean isMemberOfTopic(String topic){
        return topicSet.contains(topic);
    }

    private void hadleJoin(String[] tokens) {
        if (tokens.length > 1){
            String topic=tokens[1];
            topicSet.add(topic);
        }

    }

    // format --> (msg) (clientName->sendTow) (msgBody.....)
    // format --> (msg) (#topic" (msgBody....)
    private void handelMessage(String[] tokens) throws IOException {
        String sendTowTopic=tokens[1];
        String msgBody=tokens[2];

        boolean isTopic=sendTowTopic.charAt(0)=='#';

        List<ServerHandler> serverClientList=server.getClientList();
        for (ServerHandler serverClientHandler : serverClientList){
            if(isTopic){
                if (serverClientHandler.isMemberOfTopic(sendTowTopic)){
                    String msgOut="msg from ["+loginUser +"] Topic["+sendTowTopic+"] : "+msgBody+"\n";
                    serverClientHandler.send(msgOut);
                }
            }else {
                if (sendTowTopic.equalsIgnoreCase(serverClientHandler.getLoginUser())){
                String msgOut="msg from ["+loginUser +"]: "+msgBody+"\n";
                serverClientHandler.send(msgOut);
                }
            }

        }

    }

    private void handelLogoff() throws IOException {
        server.removeClientfromServer(this);

        List<ServerHandler> serverClientList=server.getClientList();
        String offlineMsg="offline "+loginUser+"\n";

        for (ServerHandler serverHandler : serverClientList){
            // to avoid sending online statuses to your self
            if(!loginUser.equals(serverHandler.getLoginUser())) {
                serverHandler.send(offlineMsg);
            }
        }
        clientSocket.close();
    }

    private void hadleLogin(OutputStream output, String[] tokens) throws IOException {
        if (tokens.length == 3){
            String login=tokens[1];
            String password=tokens[2];
            if ( login.equals("gust")&& password.equals("gust") || login.equals("anas")&& password.equals("anas")) {
                String msg="[Server] : Ok login"+"\n";
                output.write(msg.getBytes());
                this.loginUser=login;
                System.out.println("user Login succesfully -> "+loginUser);

                //String onLineMsg="online "+loginUser+"\n";
                List<ServerHandler> serverClientList=server.getClientList();
                // first Block -> sent to current User to all other online login
                for (ServerHandler serverHandler : serverClientList){
                    if (serverHandler.getLoginUser()!= null){
                        // to avoid sending online statuses to your self
                        if(!loginUser.equals(serverHandler.getLoginUser())) {
                            String messageTow = "online " + serverHandler.getLoginUser() + "\n";
                        send(messageTow);
                        }
                    }
                }

                //sec Block -> send other online User current user's status
                String onLineMsg="online "+loginUser+"\n";
                for (ServerHandler serverHandler : serverClientList){
                    // to avoid sending online statuses to your self
                    if(!loginUser.equals(serverHandler.getLoginUser())) {
                    serverHandler.send(onLineMsg);
                    }
                }
            }else {
                String msg="[Server] : Error login"+"\n";
                output.write(msg.getBytes());
                System.err.println("login failed ,Wrong password for : "+login);
            }
        }
    }

    private void send(String onLineMsg) throws IOException {
        if (loginUser != null){
        output.write(onLineMsg.getBytes());
        }
    }
}
