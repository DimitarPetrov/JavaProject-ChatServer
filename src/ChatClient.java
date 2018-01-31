import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class ChatClient implements Runnable {

    private static Socket clientSocket;

    private static boolean pendingFile;
    private static final String FILE_CONFIRMED_SIGNAL = "File_Confirmation_Signal_35231";
    private static final int FILE_MAX_SIZE = 1000 * 1024;

    private static PrintWriter pw;
    private static BufferedReader br;

    public static final String RESET = "\033[0m";
    public static final String GREEN_BOLD = "\033[1;32m";  // GREEN

    public static void main(String[] args) {
        System.out.println(GREEN_BOLD + "Type help to see possible commands!" + RESET);
        Scanner scanner = new Scanner(System.in);
        String line;
        String host;
        String port;
        while (true) {
            if (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if(line.equals("help")){
                    System.out.println(GREEN_BOLD + "Available Commands:" + RESET);
                    System.out.println(GREEN_BOLD +
                            "connect + host + port" +
                            " -> (default host: 192.168.0.106, default port: 1337)" + RESET);
                    continue;
                }
                if (line.startsWith("connect")) {
                    String[] split = line.split(" ");
                    if (split.length < 3 || !split[0].equals("connect")) {
                        System.out.println(GREEN_BOLD + "Wrong Command! Try again!" + RESET);
                        continue;
                    }
                    host = split[1];
                    port = split[2];
                    try {
                        clientSocket = new Socket(host, Integer.parseInt(port));
                    } catch (IOException | NumberFormatException e) {
                        System.err.println("Wrong host or port!");
                        continue;
                    }
                    System.out.println(GREEN_BOLD + "Connected to " + host + " on port: " + port + RESET);
                    break;
                }
            }
        }
        try {
            pw = new PrintWriter(clientSocket.getOutputStream());
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Problem while establishing connection through socket!");
            throw new RuntimeException(e);
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
                if (message.startsWith("send-file")) {
                    pendingFile = true;
                }
                pw.println(message);
                pw.flush();
            }
        }
        scanner.close();
    }

    private void receiveFile() {
        try {
            InputStream in = clientSocket.getInputStream();
            String path = br.readLine();
            if(path.equals("File sending unsuccessful!")){
                System.out.println(GREEN_BOLD + "File sending unsuccessful!" + RESET);
                return;
            }
            Path toSave = Paths.get(path);

            if(!Files.exists(toSave.getParent())){
                path = System.getProperty("user.home") + "/Downloads/" + toSave.getFileName();
            }
            OutputStream out = new FileOutputStream(path);
            byte[] bytes = new byte[FILE_MAX_SIZE];
            int count;
            count = in.read(bytes);
            out.write(bytes, 0, count);
            out.flush();
            out.close();
            System.out.println(GREEN_BOLD + "File successfully received!" + RESET);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        try {
            String response;
            while (true) {
                if ((response = br.readLine()) != null) {
                    if (response.equals("disconnect")) {
                        pw.close();
                        br.close();
                        clientSocket.close();
                        break;
                    }
                    if (response.equals(FILE_CONFIRMED_SIGNAL)) {
                        receiveFile();
                        continue;
                    }
                    if (pendingFile && (response.startsWith("confirm") || response.equals("cancel"))) {
                        pw.println(response);
                        pw.flush();
                        pendingFile = false;
                        continue;
                    }
                    System.out.println(GREEN_BOLD + response + RESET);
                }
            }
        } catch (IOException e) {
            System.err.println("Problem with client socket occured!");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
