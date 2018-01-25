import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ClientResponderThread extends Thread {

    private Socket socket;
    private ConcurrentHashMap<String, CommunicationService> onlineClients;

    ClientResponderThread(Socket socket, ConcurrentHashMap<String, CommunicationService> onlineClients) {
        this.socket = socket;
        this.onlineClients = onlineClients;
    }


    public void run() {
        try {
            CommunicationService communicationService =
                    new CommunicationService(socket.getOutputStream(), socket.getInputStream(), onlineClients);
            communicationService.initialize();
            communicationService.communicate();
        } catch (IOException e) {
            System.err.println("Reading or writing through socket failed!");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}