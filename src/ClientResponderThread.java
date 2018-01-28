import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ClientResponderThread extends Thread {

    private Socket socket;
    private ConcurrentHashMap<String, CommunicationService> onlineClients;
    private ConcurrentHashMap<String, ChatRoom> chatRooms;


    ClientResponderThread(Socket socket, ConcurrentHashMap<String, CommunicationService> onlineClients,
                          ConcurrentHashMap<String, ChatRoom> chatRooms) {
        this.socket = socket;
        this.onlineClients = onlineClients;
        this.chatRooms = chatRooms;
    }


    public void run() {
        try {
            CommunicationService communicationService =
                    new CommunicationService(socket.getOutputStream(), socket.getInputStream(), onlineClients, chatRooms);
            communicationService.initialize();
            communicationService.communicate();
            socket.close();
        } catch (IOException e) {
            System.err.println("Reading or writing through socket failed!");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}