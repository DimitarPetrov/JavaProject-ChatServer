import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class CommunicationService {

    private String username;
    private boolean disconnected;
    private PrintWriter pw;
    private BufferedReader br;
    private ConcurrentHashMap<String, CommunicationService> onlineClients;

    public CommunicationService(PrintWriter pw, BufferedReader br,
                                ConcurrentHashMap<String,CommunicationService> onlineClients){
        this.pw = pw;
        this.br = br;
        this.onlineClients = onlineClients;
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
            e.printStackTrace();
            throw new RuntimeException();
        } catch (IOException e) {
            throw new RuntimeException();
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
            e.printStackTrace();
            throw new RuntimeException();
        } catch (IOException e) {
            throw new RuntimeException();
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
            e.printStackTrace();
            throw new RuntimeException();
        } catch (IOException e) {
            System.err.println("Can not connect to client Socket");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private void disconnect(){
        for (String key : onlineClients.keySet()) {
            onlineClients.remove(username);
        }
        disconnected = true;
        pw.println("disconnect");
        pw.flush();
    }

    private void listUsers(){
        if(onlineClients.size() == 1){
            pw.println("There aren't any active users right now!");
            pw.flush();
        }
        for (String user : onlineClients.keySet()) {
            if(user.equals(username)){
                continue;
            }
            pw.println(user);
            pw.flush();
        }
    }

    private void sendMessage(String to, String message){
        if (onlineClients.get(to) != null) {
            onlineClients.get(to).pw.println(username + " sent you a message: " + message);
            onlineClients.get(to).pw.flush();
        } else {
            pw.println("The user " + to + " is not online at the moment!");
            pw.flush();
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
        } catch (IOException e){
            System.err.println("Reading or writing through socket failed!");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void communicate(){
        try {
            String message;
            while (!disconnected) {
                if ((message = br.readLine()) != null) {
                    if (message.equals("disconnect")) {
                        disconnect();
                        continue;
                    }
                    if (message.equals("list-users")) {
                        listUsers();
                        continue;
                    }
                    if (message.startsWith("send")) {
                        String[] split = message.split(" ");
                        sendMessage(split[1], message.substring(message.indexOf(split[2])));
                        continue;
                    }
                }
            }
        } catch (IOException e){
            System.err.println("Reading or writing through socket failed!");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommunicationService that = (CommunicationService) o;

        if (disconnected != that.disconnected) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (pw != null ? !pw.equals(that.pw) : that.pw != null) return false;
        if (br != null ? !br.equals(that.br) : that.br != null) return false;
        return onlineClients != null ? onlineClients.equals(that.onlineClients) : that.onlineClients == null;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (disconnected ? 1 : 0);
        result = 31 * result + (pw != null ? pw.hashCode() : 0);
        result = 31 * result + (br != null ? br.hashCode() : 0);
        result = 31 * result + (onlineClients != null ? onlineClients.hashCode() : 0);
        return result;
    }
}
