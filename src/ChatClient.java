import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient implements Runnable {

    private static Socket clientSocket;

    private static boolean pendingFile;
    private static final String FILE_CONFIRMED_SIGNAL = "File_Confirmation_Signal_35231";
    private static final int FILE_MAX_SIZE = 16*1024;

    private static PrintWriter pw;
    private static BufferedReader br;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            String line;
            String host;
            String port;
            while (true) {
                if (scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    if (line.startsWith("connect")) {
                        String[] split = line.split(" ");
                        if (split.length < 3) {
                            continue;
                        }
                        host = split[1];
                        port = split[2];
                        System.out.println("Connected to " + host + " on port: " + port);
                        break;
                    }
                }
            }
            clientSocket = new Socket(host, Integer.parseInt(port));
            try {
                pw = new PrintWriter(clientSocket.getOutputStream());
                br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                System.err.println("Problem while establishing connection through socket!");
                e.printStackTrace();
                throw new RuntimeException();
            }
            new Thread(new ChatClient()).start();
            String message;
            while (true) {
                if (scanner.hasNextLine()) {
                    message = scanner.nextLine();
                    if (message.equals("disconnect")) {
                        pw.println(message);
                        pw.flush();
                        break;
                    }
                    if(message.startsWith("send-file")){
                        pendingFile = true;
                    }
                    pw.println(message);
                    pw.flush();
                }
            }
        } catch (IOException e) {
            System.err.println("Poblem with client socket occurred!");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private void receiveFile(){
        try {
            InputStream in = clientSocket.getInputStream();
            OutputStream out = new FileOutputStream(br.readLine());
            byte[] bytes = new byte[FILE_MAX_SIZE];
            int count;
            count = in.read(bytes);
            out.write(bytes,0,count);
            out.flush();
            out.close();
            System.out.println("File successfully received!");
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void run() {
        try {
            String response;
            while (true) {
                if ((response = br.readLine()) != null) {
                    if (response.equals("disconnect")) {
                        break;
                    }
                    if(response.equals(FILE_CONFIRMED_SIGNAL)){
                        receiveFile();
                        continue;
                    }
                    if(pendingFile && (response.startsWith("confirm") || response.equals("cancel"))){
                        pw.println(response);
                        pw.flush();
                        pendingFile = false;
                        continue;
                    }
                    System.out.println(response);
                }
            }
        } catch (IOException e) {
            System.err.println("Problem with client socket occured!");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
