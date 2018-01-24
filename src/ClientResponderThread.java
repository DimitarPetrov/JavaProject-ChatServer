import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ClientResponderThread extends Thread{

    private Socket socket;
    private ConcurrentHashMap<String, ClientResponderThread> onlineClients;

    ClientResponderThread(Socket socket, ConcurrentHashMap<String, ClientResponderThread> onlineClients){
        this.socket = socket;
        this.onlineClients = onlineClients;
    }

    private boolean alreadyUsed(String username){
        try(BufferedReader br = new BufferedReader(new FileReader("Users.txt"))){
            String line;
            while((line = br.readLine()) != null){
                if(line.startsWith(username)){
                    return true;
                }
            }
            return false;
        } catch (FileNotFoundException e){
            System.err.println("File Users.txt missing !!!");
            e.printStackTrace();
            throw new RuntimeException();
        } catch (IOException e){
            throw new RuntimeException();
        }
    }

    private boolean login(String username, String password, PrintWriter spw){
        try(BufferedReader br = new BufferedReader(new FileReader("Users.txt"))){
            String line;
            while((line = br.readLine()) != null){
                String[] split = line.split(" ");
                if(split[0].equals(username) && split[1].equals(password)){
                    spw.println("Successfully logged in! Hello " + username + "!");
                    spw.flush();
                    return true;
                }
            }
            return false;
        } catch(FileNotFoundException e){
            System.err.println("File Users.txt missing !!!");
            e.printStackTrace();
            throw new RuntimeException();
        } catch (IOException e){
            throw new RuntimeException();
        }
    }

    private boolean registerUser(String username, String password, PrintWriter spw){
        try(PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter("Users.txt", true)))){
            if(!alreadyUsed(username)) {
                pw.println(username + " " + password);
                pw.flush();
                spw.println("User: " + username + " successfully registered!");
                return true;
            }
            return false;
        } catch (FileNotFoundException e){
            System.err.println("File Users.txt missing !!!");
            e.printStackTrace();
            throw new RuntimeException();
        } catch (IOException e){
            System.err.println("Can not connect to client Socket");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void run(){
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter pw = new PrintWriter(socket.getOutputStream())){
            String line;
            boolean disconnected = false;
            while(true){
                if((line = reader.readLine()) != null) {
                    if(line.equals("disconnect")){
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
                        String username = split[1];
                        String password = split[2];
                        if (login(username, password,pw)) {
                            onlineClients.put(username, this);
                            //onlineClients.get(username).start();
                            break;
                        } else {
                            pw.println("Wrong Username or Password! Try again!");
                            pw.flush();
                        }
                    }
                    if (line.startsWith("register")) {
                        String[] split = line.split(" ");
                        if(split.length < 3){
                            continue;
                        }
                        String username = split[1];
                        String password = split[2];
                        if(!registerUser(username, password,pw)){
                            pw.println("Username: " + username + " is already used!");
                            pw.flush();
                            continue;

                        }
                        onlineClients.put(username, this);
                        //onlineClients.get(username).start();
                        break;
                    }
                }
            }
            String message;
            while(!disconnected) {
                if ((message = reader.readLine()) != null) {
                    if(message.equals("disconnect")){
                        for(String key : onlineClients.keySet()){
                            if(onlineClients.get(key) == this){
                                onlineClients.remove(key);
                            }
                        }
                        disconnected = true;
                        pw.println("disconnect");
                        pw.flush();
                        continue;
                    }
                    if (message.equals("list-users")) {
                        for (String user : onlineClients.keySet()) {
                            pw.println(user);
                            pw.flush();
                        }
                        continue;
                    }
                }
            }

        } catch (IOException e){
            System.err.println("Reading or writing through socket failed!");
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientResponderThread that = (ClientResponderThread) o;

        if (socket != null ? !socket.equals(that.socket) : that.socket != null) return false;
        return onlineClients != null ? onlineClients.equals(that.onlineClients) : that.onlineClients == null;
    }

    @Override
    public int hashCode() {
        int result = socket != null ? socket.hashCode() : 0;
        result = 31 * result + (onlineClients != null ? onlineClients.hashCode() : 0);
        return result;
    }
}
