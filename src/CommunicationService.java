import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public class CommunicationService {

    private String username;
    private boolean disconnected;
    private ConcurrentHashMap<String, CommunicationService> onlineClients;

    private PrintWriter pw;
    private BufferedReader br;

    private OutputStream os;
    private InputStream is;

    private boolean filePending;
    private static final int FILE_MAX_SIZE = 16*1024;
    private static final String FILE_CONFIRMED_SIGNAL = "File_Confirmation_Signal_35231";

    public CommunicationService(OutputStream os, InputStream is,
                                ConcurrentHashMap<String, CommunicationService> onlineClients) {
        this.os = os;
        this.is = is;
        this.pw = new PrintWriter(os);
        this.br = new BufferedReader(new InputStreamReader(is));
        this.onlineClients = onlineClients;
    }

    public CommunicationService(PrintWriter pw, BufferedReader br,
                                ConcurrentHashMap<String, CommunicationService> onlineClients){
        this.pw = pw;
        this.br = br;
        this.onlineClients = onlineClients;
        os = null;
        is = null;
    }

    private boolean alreadyUsed(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader("Users.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(username)) {
                    return true;
                }
            }
            return false;
        } catch (FileNotFoundException e) {
            System.err.println("File Users.txt missing !!!");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean login(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader("Users.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split(" ");
                if (split[0].equals(username) && split[1].equals(password)) {
                    pw.println("Successfully logged in! Hello " + username + "!");
                    pw.flush();
                    return true;
                }
            }
            return false;
        } catch (FileNotFoundException e) {
            System.err.println("File Users.txt missing !!!");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean registerUser(String username, String password) {
        try (PrintWriter fw = new PrintWriter(new BufferedWriter(new FileWriter("Users.txt", true)))) {
            if (!alreadyUsed(username)) {
                fw.println(username + " " + password);
                fw.flush();
                pw.println("User: " + username + " successfully registered!");
                return true;
            }
            return false;
        } catch (FileNotFoundException e) {
            System.err.println("File Users.txt missing !!!");
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.err.println("Can not connect to client Socket");
            throw new RuntimeException(e);
        }
    }

    private void disconnect() {
        for (String key : onlineClients.keySet()) {
            onlineClients.remove(username);
        }
        disconnected = true;
        pw.println("disconnect");
        pw.flush();
    }

    private void listUsers() {
        if (onlineClients.size() == 1) {
            pw.println("There aren't any active users right now!");
            pw.flush();
        }
        for (String user : onlineClients.keySet()) {
            if (user.equals(username)) {
                continue;
            }
            pw.println(user);
            pw.flush();
        }
    }

    private boolean sendMessage(String to, String message) {
        if (onlineClients.get(to) != null) {
            onlineClients.get(to).pw.println(username + " sent you a message: " + message);
            onlineClients.get(to).pw.flush();
            return true;
        } else {
            pw.println("The user " + to + " is not online at the moment!");
            pw.flush();
            return false;
        }
    }

    private boolean confirmFile(String message){
        String[] split = message.split(" ");
        if(split.length < 3){
            return false;
        }
        onlineClients.get(split[1]).pw.println("confirm " + message.substring(message.indexOf(split[2])));
        onlineClients.get(split[1]).pw.flush();
        pw.println(FILE_CONFIRMED_SIGNAL);
        pw.flush();
        filePending = false;
        return true;
    }

    private boolean cancelFile(String message){
        String[] split = message.split(" ");
        if(split.length < 2){
            return false;
        }
        onlineClients.get(split[1]).pw.println("cancel");
        onlineClients.get(split[1]).pw.flush();
        filePending = false;
        return true;
    }

    private boolean sendFile(String to, Path filePath, Path pathToSave){
        try {
            CommunicationService receiver = onlineClients.get(to);
            receiver.pw.println(pathToSave.toString() + '/' + filePath.getFileName());
            receiver.pw.flush();
            byte[] bytes = new byte[FILE_MAX_SIZE];
            File file = filePath.toFile();
            if(file.length() >= FILE_MAX_SIZE){
                return false;
            }
            InputStream in = new FileInputStream(file);
            int count;
            count = in.read(bytes);
            receiver.os.write(bytes, 0, count);
            receiver.os.flush();
            in.close();
            pw.println("File sent successfully!");
            pw.flush();
            return true;
        } catch (FileNotFoundException e){
            pw.println("There is no such a file!");
            pw.flush();
            throw new RuntimeException(e);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private boolean fileReceiverAuthentication(String message){
        try {
            String[] split = message.split(" ");
            if (split.length < 3) {
                return false;
            }
            String receiver = split[1];
            Path filePath = Paths.get(message.substring(message.indexOf(split[2])));
            if(!sendMessage(receiver, username + " want to send you a file "
                    + filePath + "!\nType confirm, sender's name and a path to accept" +
                    " or cancel and sender's name to decline!")){
                return false;
            }
            onlineClients.get(receiver).filePending = true;
            String response;
            while ((response = br.readLine()) != null) {
                if (response.equals("cancel")) {
                    pw.println("The file was canceled by " + receiver + "!");
                    pw.flush();
                    break;
                }
                if (response.startsWith("confirm")) {
                    String[] strings = response.split(" ");
                    if(strings.length < 2){
                        return false;
                    }
                    if(!sendFile(receiver, filePath,
                                Paths.get(response.substring(response.indexOf(strings[1]))))){
                        pw.println("The file is too large!");
                        pw.flush();
                        onlineClients.get(receiver).pw.println("The file is too large");
                        onlineClients.get(receiver).pw.flush();
                    }
                    break;
                }
            }
            return true;
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void initialize() {
        try {
            String line;
            while (true) {
                if ((line = br.readLine()) != null) {
                    if (line.equals("disconnect")) {
                        disconnected = true;
                        pw.println("disconnect");
                        pw.flush();
                        break;
                    }
                    if (line.startsWith("login")) {
                        String[] split = line.split(" ");
                        if (split.length < 3) {
                            continue;
                        }
                        username = split[1];
                        String password = split[2];
                        if (login(username, password)) {
                            onlineClients.put(username, this);
                            break;
                        } else {
                            pw.println("Wrong Username or Password! Try again!");
                            pw.flush();
                        }
                    }
                    if (line.startsWith("register")) {
                        String[] split = line.split(" ");
                        if (split.length < 3) {
                            continue;
                        }
                        username = split[1];
                        String password = split[2];
                        if (!registerUser(username, password)) {
                            pw.println("Username: " + username + " is already used!");
                            pw.flush();
                            continue;
                        }
                        onlineClients.put(username, this);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Reading or writing through socket failed!");
            throw new RuntimeException(e);
        }
    }

    public void communicate() {
        try {
            String message;
            while (!disconnected) {
                if ((message = br.readLine()) != null) {
                    if(filePending) {
                        do {
                            if (message.startsWith("confirm")) {
                                if(confirmFile(message)){
                                    break;
                                } else {
                                    pw.println("Wrong command! Try again!");
                                    pw.flush();
                                    continue;
                                }
                            }
                            if (message.startsWith("cancel")) {
                                if(cancelFile(message)){
                                    pw.println("File canceled!");
                                    pw.flush();
                                    break;
                                } else {
                                    pw.println("Wrong command! Try again!");
                                    pw.flush();
                                    continue;
                                }
                            }
                        }
                        while ((message = br.readLine()) != null);
                        continue;
                    }
                    if (message.equals("disconnect")) {
                        disconnect();
                        continue;
                    }
                    if (message.equals("list-users")) {
                        listUsers();
                        continue;
                    }
                    if (message.startsWith("send-file")) {
                        if(!fileReceiverAuthentication(message)){
                            pw.println("There was a problem with sending your file!");
                            pw.flush();
                        }
                        continue;
                    }
                    if (message.startsWith("send")) {
                        String[] split = message.split(" ");
                        if(split.length < 3){
                            pw.println("There was a problem with sending your message! Probably wrong command!");
                            pw.flush();
                            continue;
                        }
                        sendMessage(split[1], message.substring(message.indexOf(split[2])));
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Reading or writing through socket failed!");
            throw new RuntimeException(e);
        }
    }
}