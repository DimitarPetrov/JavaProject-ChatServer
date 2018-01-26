import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public class CommunicationService {

    private String username;
    private boolean disconnected;
    private ConcurrentHashMap<String, CommunicationService> onlineClients;
    private ConcurrentHashMap<String, ChatRoom> chatRooms;

    private PrintWriter pw;
    private BufferedReader br;

    private OutputStream os;
    private InputStream is;

    private boolean filePending;
    private static final int FILE_MAX_SIZE = 16 * 1024;
    private static final String FILE_CONFIRMED_SIGNAL = "File_Confirmation_Signal_35231";

    public CommunicationService(OutputStream os, InputStream is,
                                ConcurrentHashMap<String, CommunicationService> onlineClients,
                                ConcurrentHashMap<String, ChatRoom> chatRooms) {
        this.os = os;
        this.is = is;
        this.pw = new PrintWriter(os);
        this.br = new BufferedReader(new InputStreamReader(is));
        this.onlineClients = onlineClients;
        this.chatRooms = chatRooms;
    }

    public CommunicationService(PrintWriter pw, BufferedReader br,
                                ConcurrentHashMap<String, CommunicationService> onlineClients,
                                ConcurrentHashMap<String, ChatRoom> chatRooms) {
        this.pw = pw;
        this.br = br;
        this.onlineClients = onlineClients;
        this.chatRooms = chatRooms;
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

    private boolean confirmFile(String message) {
        String[] split = message.split(" ");
        if (split.length < 3 || !split[0].equals("confirm")) {
            return false;
        }
        onlineClients.get(split[1]).pw.println("confirm " + message.substring(message.indexOf(split[2])));
        onlineClients.get(split[1]).pw.flush();
        pw.println(FILE_CONFIRMED_SIGNAL);
        pw.flush();
        filePending = false;
        return true;
    }

    private boolean cancelFile(String message) {
        String[] split = message.split(" ");
        if (split.length < 2 || !split[0].equals("cancel")) {
            return false;
        }
        onlineClients.get(split[1]).pw.println("cancel");
        onlineClients.get(split[1]).pw.flush();
        filePending = false;
        return true;
    }

    private boolean sendFile(String to, Path filePath, Path pathToSave) {
        try {
            if(!Files.exists(pathToSave) || !Files.exists(filePath)){
                return false;
            }
            CommunicationService receiver = onlineClients.get(to);
            receiver.pw.println(pathToSave.toString() + '/' + filePath.getFileName());
            receiver.pw.flush();
            byte[] bytes = new byte[FILE_MAX_SIZE];
            File file = filePath.toFile();
            if (file.length() >= FILE_MAX_SIZE) {
                pw.println("The file is too large!");
                pw.flush();
                onlineClients.get(to).pw.println("The file is too large!");
                onlineClients.get(to).pw.flush();
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
        } catch (FileNotFoundException e) {
            pw.println("There is no such a file!");
            pw.flush();
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean fileReceiverAuthentication(String receiver, Path filePath) {
        try {
            if (!sendMessage(receiver, username + " want to send you a file "
                    + filePath + "!\nType confirm, sender's name and a path to accept" +
                    " or cancel and sender's name to decline!")) {
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
                    if (strings.length < 2) {
                        return false;
                    }
                    if (!sendFile(receiver, filePath,
                            Paths.get(response.substring(response.indexOf(strings[1]))))) {
                        pw.println("File sending unsuccessful!");
                        pw.flush();
                        onlineClients.get(receiver).pw.println("File sending unsuccessful!");
                        onlineClients.get(receiver).pw.flush();
                    }
                    break;
                }
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createRoom(String roomName) {
        try {
            ChatRoom chatRoom = new ChatRoom(username, onlineClients);
            chatRoom.addMember(this);
            chatRooms.put(roomName, chatRoom);
            Files.createFile(Paths.get(roomName + "History.txt"));
            pw.println("Chat room " + roomName + " successfully created!");
            pw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteRoom(String roomName) {
        try {
            if (chatRooms.get(roomName) == null) {
                pw.println("There is not such a room!");
                pw.flush();
                return;
            }
            if (username.equals(chatRooms.get(roomName).getCreator())) {
                chatRooms.remove(roomName);
                Files.delete(Paths.get(roomName + "History.txt"));
                pw.println("The room " + roomName + " successfully deleted!");
                pw.flush();
            } else {
                pw.println("You don't have permission to delete this room!");
                pw.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void listRooms() {
        boolean flag = false;
        for (String room : chatRooms.keySet()) {
            if (chatRooms.get(room).isActive()) {
                pw.println(room);
                pw.flush();
                flag = true;
            }
        }
        if (!flag) {
            pw.println("There aren't any active chat-rooms!");
            pw.flush();
        }
    }

    private void joinRoom(String roomName) {
        if (chatRooms.get(roomName) == null) {
            pw.println("There is not such a room!");
            pw.flush();
            return;
        }
        try (BufferedReader historyFile = new BufferedReader(new FileReader(roomName + "History.txt"))) {
            chatRooms.get(roomName).addMember(this);
            pw.println("You have successfully joined room " + roomName + "! Welcome!");
            pw.flush();
            String line;
            while ((line = historyFile.readLine()) != null) {
                pw.println(line);
            }
            pw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void leaveRoom(String roomName) {
        if (chatRooms.get(roomName) == null) {
            pw.println("There is not such a room!");
            pw.flush();
            return;
        }
        if (!chatRooms.get(roomName).removeMember(this)) {
            pw.println("You are not a member of room: " + roomName + "!");
            pw.flush();
        } else {
            pw.println("You have left room " + roomName + "!");
            pw.flush();
        }
    }

    private void listUsersFromRoom(String roomName) {
        if (chatRooms.get(roomName) == null) {
            pw.println("There is not such a room!");
            pw.flush();
            return;
        }
        ChatRoom chatRoom = chatRooms.get(roomName);
        boolean flag = false;
        for (String user : onlineClients.keySet()) {
            if (chatRoom.isMember(onlineClients.get(user))) {
                pw.println(user);
                pw.flush();
                flag = true;
            }
        }
        if(!flag){
            pw.println("There is not any online members in room: " + roomName +"!");
            pw.flush();
        }
    }

    private void sendToRoom(String roomName, String message) {
        ChatRoom chatRoom = chatRooms.get(roomName);
        if (chatRoom == null) {
            pw.println("There is no such a room!");
            pw.flush();
            return;
        }
        for (CommunicationService client : onlineClients.values()) {
            if (chatRoom.isMember(client) && client != this) {
                client.pw.println(username + " sent a message to your room " + roomName + ": " + message);
                client.pw.flush();
            }
        }
        try (PrintWriter fileHistory = new PrintWriter(new FileWriter(roomName + "History.txt", true))) {
            fileHistory.println(username + " sent a message to your room " + roomName + ": " + message);
            fileHistory.flush();
        } catch (IOException e) {
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
                        if (split.length < 3 || !split[0].equals("login")) {
                            pw.println("Wrong command! Try again!");
                            pw.flush();
                            continue;
                        }
                        username = split[1];
                        String password = split[2];
                        if(onlineClients.containsKey(username)){
                            pw.println("Already logged in! Please close previous session!");
                            pw.flush();
                            continue;
                        }
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
                        if (split.length < 3 || !split[0].equals("register")) {
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
                    if (filePending) {
                        do {
                            if (message.startsWith("confirm")) {
                                if (confirmFile(message)) {
                                    break;
                                } else {
                                    pw.println("Wrong command! Try again!");
                                    pw.flush();
                                    continue;
                                }
                            }
                            if (message.startsWith("cancel")) {
                                if (cancelFile(message)) {
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
                        String[] split = message.split(" ");
                        if (split.length < 3 || !split[0].equals("send-file")) {
                            pw.println("Wrong command! Try again!");
                            pw.flush();
                            continue;
                        }
                        String receiver = split[1];
                        if(receiver.equals(username)){
                            pw.println("You can not send a file to yourself!");
                            pw.flush();
                            continue;
                        }
                        Path filePath = Paths.get(message.substring(message.indexOf(split[2])));
                        if (!fileReceiverAuthentication(receiver,filePath)) {
                            pw.println("There was a problem with sending your file!");
                            pw.flush();
                        }
                        continue;
                    }
                    if (message.startsWith("send-room")) {
                        String[] split = message.split(" ");
                        if (split.length < 3 || !split[0].equals("send-room")) {
                            pw.println("Wrong command! Try again!");
                            pw.flush();
                            continue;
                        }
                        String roomName = split[1];
                        String m = message.substring(message.indexOf(split[2]));
                        sendToRoom(roomName, m);
                        continue;
                    }
                    if (message.startsWith("send")) {
                        String[] split = message.split(" ");
                        if (split.length < 3 || !split[0].equals("send")) {
                            pw.println("There was a problem with sending your message! Probably wrong command!");
                            pw.flush();
                            continue;
                        }
                        sendMessage(split[1], message.substring(message.indexOf(split[2])));
                        continue;
                    }
                    if (message.startsWith("create-room")) {
                        String[] split = message.split(" ");
                        if (split.length < 2 || !split[0].equals("create-room")) {
                            pw.println("Wrong command! Try again!");
                            pw.flush();
                            continue;
                        }
                        String roomName = message.substring(message.indexOf(split[1]));
                        if(chatRooms.containsKey(roomName)){
                            pw.println("Chat room with name " + roomName + " already exists!");
                            pw.flush();
                            continue;
                        }
                        createRoom(roomName);
                        continue;
                    }
                    if (message.startsWith("delete-room")) {
                        String[] split = message.split(" ");
                        if (split.length < 2 || !split[0].equals("delete-room")) {
                            pw.println("Wrong command! Try again!");
                            pw.flush();
                            continue;
                        }
                        String roomName = message.substring(message.indexOf(split[1]));
                        deleteRoom(roomName);
                        continue;
                    }
                    if (message.equals("list-rooms")) {
                        listRooms();
                        continue;
                    }
                    if (message.startsWith("join-room")) {
                        String[] split = message.split(" ");
                        if (split.length < 2 || !split[0].equals("join-room")) {
                            pw.println("Wrong command! Try again!");
                            pw.flush();
                            continue;
                        }
                        String roomName = message.substring(message.indexOf(split[1]));
                        joinRoom(roomName);
                        continue;
                    }
                    if (message.startsWith("leave-room")) {
                        String[] split = message.split(" ");
                        if (split.length < 2 || !split[0].equals("leave-room")) {
                            pw.println("Wrong command! Try again!");
                            pw.flush();
                            continue;
                        }
                        String roomName = message.substring(message.indexOf(split[1]));
                        leaveRoom(roomName);
                        continue;
                    }
                    if (message.startsWith("list-users")) {
                        String[] split = message.split(" ");
                        if (split.length < 2 || !split[0].equals("list-users")) {
                            pw.println("Wrong command! Try again!");
                            pw.flush();
                            continue;
                        }
                        String roomName = message.substring(message.indexOf(split[1]));
                        listUsersFromRoom(roomName);
                        continue;
                    }

                }
            }
        } catch (IOException e) {
            System.err.println("Reading or writing through socket failed!");
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommunicationService that = (CommunicationService) o;

        return username != null ? username.equals(that.username) : that.username == null;
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }
}
