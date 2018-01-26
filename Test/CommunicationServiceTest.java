import org.junit.Test;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class CommunicationServiceTest {

    @Test
    public void LogInTest(){
        try {
            PrintWriter fw = new PrintWriter("InputTest.txt");
            fw.println("login dimitar 1234");
            fw.println("disconnect");
            fw.close();
            BufferedReader br = new BufferedReader(new FileReader("InputTest.txt"));
            PrintWriter pw = new PrintWriter("OutputTest.txt");
            ConcurrentHashMap<String,CommunicationService> onlineClients = new ConcurrentHashMap<>();
            ConcurrentHashMap<String,ChatRoom> chatRooms = new ConcurrentHashMap<>();
            CommunicationService communicationService = new CommunicationService(pw,br,onlineClients,chatRooms);
            communicationService.initialize();
            br.close();
            pw.close();
            BufferedReader fr = new BufferedReader(new FileReader("OutputTest.txt"));
            String line = fr.readLine();
            assertEquals("Successfully logged in! Hello dimitar!", line);

        } catch (FileNotFoundException e){
            System.err.println("InputTest or OutputTest file is not found");
            fail();
        } catch (IOException e){
            System.err.println("Undefined behavior");
            fail();
        }
    }

    @Test
    public void LogInFailedTest(){
        try {
            PrintWriter fw = new PrintWriter("InputTest.txt");
            fw.println("login dimitar 1345894");
            fw.println("disconnect");
            fw.close();
            BufferedReader br = new BufferedReader(new FileReader("InputTest.txt"));
            PrintWriter pw = new PrintWriter("OutputTest.txt");
            ConcurrentHashMap<String,CommunicationService> onlineClients = new ConcurrentHashMap<>();
            ConcurrentHashMap<String,ChatRoom> chatRooms = new ConcurrentHashMap<>();
            CommunicationService communicationService = new CommunicationService(pw,br,onlineClients,chatRooms);
            communicationService.initialize();
            br.close();
            pw.close();
            BufferedReader fr = new BufferedReader(new FileReader("OutputTest.txt"));
            String line = fr.readLine();
            assertEquals("Wrong Username or Password! Try again!", line);

        } catch (FileNotFoundException e){
            System.err.println("InputTest or OutputTest file is not found");
            fail();
        } catch (IOException e){
            System.err.println("Undefined behavior");
            fail();
        }
    }

    @Test
    public void RegisterFailedTest(){
        try {
            PrintWriter fw = new PrintWriter("InputTest.txt");
            fw.println("register dimitar 1234");
            fw.println("disconnect");
            fw.close();
            BufferedReader br = new BufferedReader(new FileReader("InputTest.txt"));
            PrintWriter pw = new PrintWriter("OutputTest.txt");
            ConcurrentHashMap<String,CommunicationService> onlineClients = new ConcurrentHashMap<>();
            ConcurrentHashMap<String,ChatRoom> chatRooms = new ConcurrentHashMap<>();
            CommunicationService communicationService = new CommunicationService(pw,br,onlineClients,chatRooms);
            communicationService.initialize();
            br.close();
            pw.close();
            BufferedReader fr = new BufferedReader(new FileReader("OutputTest.txt"));
            String line = fr.readLine();
            assertEquals("Username: dimitar is already used!", line);

        } catch (FileNotFoundException e){
            System.err.println("InputTest or OutputTest file is not found");
            fail();
        } catch (IOException e){
            System.err.println("Undefined behavior");
            fail();
        }
    }

    @Test
    public void listUsersTest(){
        try {
            PrintWriter fw = new PrintWriter("InputTest.txt");
            fw.println("login dimitar 1234");
            fw.println("login ivan 1234");
            fw.println("list-users");
            fw.println("disconnect");
            fw.close();
            BufferedReader br = new BufferedReader(new FileReader("InputTest.txt"));
            PrintWriter pw = new PrintWriter("OutputTest.txt");
            ConcurrentHashMap<String,CommunicationService> onlineClients = new ConcurrentHashMap<>();
            ConcurrentHashMap<String,ChatRoom> chatRooms = new ConcurrentHashMap<>();
            CommunicationService communicationService = new CommunicationService(pw,br,onlineClients,chatRooms);
            communicationService.initialize();
            CommunicationService communicationService2 = new CommunicationService(pw,br,onlineClients,chatRooms);
            communicationService2.initialize();
            communicationService2.communicate();
            br.close();
            pw.close();
            BufferedReader fr = new BufferedReader(new FileReader("OutputTest.txt"));
            String line = fr.readLine();
            assertEquals("Successfully logged in! Hello dimitar!", line);
            line = fr.readLine();
            assertEquals("Successfully logged in! Hello ivan!", line);
            line = fr.readLine();
            assertEquals("dimitar", line);
        } catch (FileNotFoundException e){
            System.err.println("InputTest or OutputTest file is not found");
            fail();
        } catch (IOException e){
            System.err.println("Undefined behavior");
            fail();
        }
    }

    @Test
    public void SendMessageTest(){
        try {
            PrintWriter fw = new PrintWriter("InputTest.txt");
            fw.println("login dimitar 1234");
            fw.println("login ivan 1234");
            fw.println("send dimitar zdr");
            fw.println("disconnect");
            fw.close();
            BufferedReader br = new BufferedReader(new FileReader("InputTest.txt"));
            PrintWriter pw = new PrintWriter("OutputTest.txt");
            ConcurrentHashMap<String,CommunicationService> onlineClients = new ConcurrentHashMap<>();
            ConcurrentHashMap<String,ChatRoom> chatRooms = new ConcurrentHashMap<>();
            CommunicationService communicationService = new CommunicationService(pw,br,onlineClients,chatRooms);
            communicationService.initialize();
            CommunicationService communicationService2 = new CommunicationService(pw,br,onlineClients,chatRooms);
            communicationService2.initialize();
            communicationService2.communicate();
            br.close();
            pw.close();
            BufferedReader fr = new BufferedReader(new FileReader("OutputTest.txt"));
            String line = fr.readLine();
            assertEquals("Successfully logged in! Hello dimitar!", line);
            line = fr.readLine();
            assertEquals("Successfully logged in! Hello ivan!", line);
            line = fr.readLine();
            assertEquals("ivan sent you a message: zdr", line);
        } catch (FileNotFoundException e){
            System.err.println("InputTest or OutputTest file is not found");
            fail();
        } catch (IOException e){
            System.err.println("Undefined behavior");
            fail();
        }
    }

    @Test
    public void SendMessageNotActiveUserTest(){
        try {
            PrintWriter fw = new PrintWriter("InputTest.txt");
            fw.println("login dimitar 1234");
            fw.println("send ivan zdr");
            fw.println("disconnect");
            fw.close();
            BufferedReader br = new BufferedReader(new FileReader("InputTest.txt"));
            PrintWriter pw = new PrintWriter("OutputTest.txt");
            ConcurrentHashMap<String,CommunicationService> onlineClients = new ConcurrentHashMap<>();
            ConcurrentHashMap<String,ChatRoom> chatRooms = new ConcurrentHashMap<>();
            CommunicationService communicationService = new CommunicationService(pw,br,onlineClients,chatRooms);
            communicationService.initialize();
            communicationService.communicate();
            br.close();
            pw.close();
            BufferedReader fr = new BufferedReader(new FileReader("OutputTest.txt"));
            String line = fr.readLine();
            assertEquals("Successfully logged in! Hello dimitar!", line);
            line = fr.readLine();
            assertEquals("The user ivan is not online at the moment!", line);
        } catch (FileNotFoundException e){
            System.err.println("InputTest or OutputTest file is not found");
            fail();
        } catch (IOException e){
            System.err.println("Undefined behavior");
            fail();
        }
    }

    @Test
    public void SendFileCancelTest(){
        try {
            PrintWriter fw = new PrintWriter("InputTest.txt");
            fw.println("login dimitar 1234");
            fw.println("login ivan 1234");
            fw.println("send-file dimitar asd");
            fw.println("cancel");
            fw.println("disconnect");
            fw.close();
            BufferedReader br = new BufferedReader(new FileReader("InputTest.txt"));
            PrintWriter pw = new PrintWriter("OutputTest.txt");
            ConcurrentHashMap<String,CommunicationService> onlineClients = new ConcurrentHashMap<>();
            ConcurrentHashMap<String,ChatRoom> chatRooms = new ConcurrentHashMap<>();
            CommunicationService communicationService = new CommunicationService(pw,br,onlineClients,chatRooms);
            CommunicationService communicationService2 = new CommunicationService(pw,br,onlineClients,chatRooms);
            communicationService.initialize();
            communicationService2.initialize();
            communicationService2.communicate();
            br.close();
            pw.close();
            BufferedReader fr = new BufferedReader(new FileReader("OutputTest.txt"));
            String line = fr.readLine();
            assertEquals("Successfully logged in! Hello dimitar!", line);
            line = fr.readLine();
            assertEquals("Successfully logged in! Hello ivan!", line);
            line = fr.readLine();
            assertEquals("ivan sent you a message: ivan want to send you a file asd!", line);
            line = fr.readLine();
            assertEquals("Type confirm, sender's name and a path to accept or cancel and sender's name to decline!", line);
            line = fr.readLine();
            assertEquals("The file was canceled by dimitar!", line);
        } catch (FileNotFoundException e){
            System.err.println("InputTest or OutputTest file is not found");
            fail();
        } catch (IOException e){
            System.err.println("Undefined behavior");
            fail();
        }
    }

    @Test(expected = RuntimeException.class)
    public void SendFileConfirmTestNotExistingFile(){
        try {
            PrintWriter fw = new PrintWriter("InputTest.txt");
            fw.println("login dimitar 1234");
            fw.println("login ivan 1234");
            fw.println("send-file dimitar asd");
            fw.println("confirm dsa");
            fw.println("disconnect");
            fw.close();
            BufferedReader br = new BufferedReader(new FileReader("InputTest.txt"));
            PrintWriter pw = new PrintWriter("OutputTest.txt");
            ConcurrentHashMap<String,CommunicationService> onlineClients = new ConcurrentHashMap<>();
            ConcurrentHashMap<String,ChatRoom> chatRooms = new ConcurrentHashMap<>();
            CommunicationService communicationService = new CommunicationService(pw,br,onlineClients,chatRooms);
            CommunicationService communicationService2 = new CommunicationService(pw,br,onlineClients,chatRooms);
            communicationService.initialize();
            communicationService2.initialize();
            communicationService2.communicate();
            br.close();
            pw.close();
            BufferedReader fr = new BufferedReader(new FileReader("OutputTest.txt"));
            String line = fr.readLine();
            assertEquals("Successfully logged in! Hello dimitar!", line);
            line = fr.readLine();
            assertEquals("Successfully logged in! Hello ivan!", line);
            line = fr.readLine();
            assertEquals("ivan sent you a message: ivan want to send you a file asd!", line);
            line = fr.readLine();
            assertEquals("Type confirm, sender's name and a path to accept or cancel and sender's name to decline!", line);
            line = fr.readLine();
            assertEquals("There is no such a file!", line);
        } catch (FileNotFoundException e){
            System.err.println("InputTest or OutputTest file is not found");
            fail();
        } catch (IOException e){
            System.err.println("Undefined behavior");
            fail();
        }
    }

    @Test(expected = NullPointerException.class)
    public void SendFileConfirmTestExistingFile(){
        try {
            PrintWriter fw = new PrintWriter("InputTest.txt");
            fw.println("login dimitar 1234");
            fw.println("login ivan 1234");
            fw.println("send-file dimitar /home/dimitar/IdeaProjects/Java Practicum/ChatServer/InputTest.txt");
            fw.println("confirm /home/dimitar");
            fw.println("disconnect");
            fw.close();
            BufferedReader br = new BufferedReader(new FileReader("InputTest.txt"));
            PrintWriter pw = new PrintWriter("OutputTest.txt");
            ConcurrentHashMap<String,CommunicationService> onlineClients = new ConcurrentHashMap<>();
            ConcurrentHashMap<String,ChatRoom> chatRooms = new ConcurrentHashMap<>();
            CommunicationService communicationService = new CommunicationService(pw,br,onlineClients,chatRooms);
            CommunicationService communicationService2 = new CommunicationService(pw,br,onlineClients,chatRooms);
            communicationService.initialize();
            communicationService2.initialize();
            communicationService2.communicate();
            br.close();
            pw.close();
            BufferedReader fr = new BufferedReader(new FileReader("OutputTest.txt"));
            String line = fr.readLine();
            assertEquals("Successfully logged in! Hello dimitar!", line);
            line = fr.readLine();
            assertEquals("Successfully logged in! Hello ivan!", line);
            line = fr.readLine();
            assertEquals("ivan sent you a message: ivan want to send you a file asd!", line);
            line = fr.readLine();
            assertEquals("Type confirm, sender's name and a path to accept or cancel and sender's name to decline!", line);
            line = fr.readLine();
            assertEquals("/home/dimitar/InputTest.txt", line);
        } catch (FileNotFoundException e){
            System.err.println("InputTest or OutputTest file is not found");
            fail();
        } catch (IOException e){
            System.err.println("Undefined behavior");
            fail();
        }
    }
}