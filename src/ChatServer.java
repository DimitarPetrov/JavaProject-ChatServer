import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

    public static final int SERVER_PORT = 1337;

    private static ConcurrentHashMap<String, ClientResponderThread> onlineClients= new ConcurrentHashMap<>();



    public static void main(String[] args){
        try(ServerSocket ss = new ServerSocket(SERVER_PORT)){
            while(true){
                Socket clientSocket = ss.accept();
                new ClientResponderThread(clientSocket,onlineClients).start();

            }

        } catch (IOException e){
            System.err.println("ChatServer socket problem occurred!");
            e.printStackTrace();
        }
    }
}
