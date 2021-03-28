package com.nado;

import com.nado.listner.MessageListenner;
import com.nado.listner.UserStatusListener;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private final String localhost;
    private final int port;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BufferedReader bufferedReader;

    private ArrayList<UserStatusListener> userStatusListenerArrayList=new ArrayList<>();
    private ArrayList<MessageListenner> messageListenerArrayList =new ArrayList<>();

    public ChatClient(String localhost, int port) {
        this.localhost=localhost;
        this.port=port;
    }

    public static void main(String[] args) throws IOException {
        ChatClient client=new ChatClient("localhost",9090);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String userOnline) {
                System.out.println("ONLINE : "+userOnline);
            }

            @Override
            public void offline(String userOffline) {
                System.out.println("OFFLINE : "+userOffline);
            }
        });
        //Test MessageListener
        client.addMessageListenner(new MessageListenner() {
            @Override
            public void onMessage(String fromLogin, String messageBody) {
                System.out.println("You got message "+fromLogin+messageBody);
            }
        });
        if (!client.connect()){
            System.err.println("Connected failed");
        }else {
            System.out.println("Connected successes");
            if (client.login("anas","anas")){
                System.out.println("user Login Successful");
                //Test
                client.msg("gust","hi iam anas");
            }else
                System.err.println("user Login Failed , Password uncorrected");
        }
        //client.logoff();
    }

    private void msg(String sentTow, String msgBody) throws IOException {
        String cmd="msg "+sentTow+" "+msgBody+"\n";
        outputStream.write(cmd.getBytes());
    }

    private void logoff() throws IOException {
        String cmd="logoff";
        outputStream.write(cmd.getBytes());
    }

    private boolean login(String username, String passwoard) throws IOException {
        String cmd="login "+ username +" "+passwoard+"\n";
        outputStream.write(cmd.getBytes());
        String response=bufferedReader.readLine();
        System.out.println("response --> "+response);

        if ("[Server] : Ok login".equalsIgnoreCase(response)){
            statrtMessageReaderFromServer();
            return true;
        }else {
            return false;
        }

    }

    private void readMessageLoop(){
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens= StringUtils.split(line);
                if (tokens != null && tokens.length>0){
                    String cmd=tokens[0];
                    if ("online".equalsIgnoreCase(cmd)){
                        handelOnline(tokens);
                    }else if("offline".equalsIgnoreCase(cmd)){
                        handelOffline(tokens);
                    }else if("msg".equalsIgnoreCase(cmd)){
                        String[]tokenMessage=StringUtils.split(line,null,3);
                        handelMessage(tokenMessage);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void handelMessage(String[] tokenMessage) {
        String login=tokenMessage[1];
        String msgBody=tokenMessage[2];

        for (MessageListenner messageListenner:messageListenerArrayList) {
            messageListenner.onMessage(login,msgBody);
        }
    }

    private void handelOffline(String[] tokens) {
        String logout=tokens[1];
        for (UserStatusListener userStatusListener:userStatusListenerArrayList){
            userStatusListener.offline(logout);
        }
    }

    private void handelOnline(String[] tokens) {
        String login=tokens[1];
        for (UserStatusListener userStatusListener:userStatusListenerArrayList){
            userStatusListener.online(login);
        }
    }

    private void statrtMessageReaderFromServer() {
        Thread thread=new Thread(){
            @Override
            public void run() {
                readMessageLoop();
            }
        };thread.start();
    }

    private boolean connect() {
        try {
            this.socket=new Socket(localhost,port);
            this.outputStream=socket.getOutputStream();
            this.inputStream=socket.getInputStream();

            this.bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addUserStatusListener(UserStatusListener listener){
        userStatusListenerArrayList.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener){
        userStatusListenerArrayList.remove(listener);
    }

    public void addMessageListenner(MessageListenner messageListenner){
        messageListenerArrayList.add(messageListenner);
    }

    public void removeMessageListenner(MessageListenner messageListenner){
        messageListenerArrayList.remove(messageListenner);
    }
}
