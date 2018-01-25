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
            CommunicationService communicationService = new CommunicationService(pw,br,onlineClients);
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
            CommunicationService communicationService = new CommunicationService(pw,br,onlineClients);
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
            CommunicationService communicationService = new CommunicationService(pw,br,onlineClients);
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
            fw.println("login ivan 23228");
            fw.println("list-users");
            fw.println("disconnect");
            fw.close();
            BufferedReader br = new BufferedReader(new FileReader("InputTest.txt"));
            PrintWriter pw = new PrintWriter("OutputTest.txt");
            ConcurrentHashMap<String,CommunicationService> onlineClients = new ConcurrentHashMap<>();
            CommunicationService communicationService = new CommunicationService(pw,br,onlineClients);
            communicationService.initialize();
            CommunicationService communicationService2 = new CommunicationService(pw,br,onlineClients);
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
            fw.println("login ivan 23228");
            fw.println("send dimitar zdr");
            fw.println("disconnect");
            fw.close();
            BufferedReader br = new BufferedReader(new FileReader("InputTest.txt"));
            PrintWriter pw = new PrintWriter("OutputTest.txt");
            ConcurrentHashMap<String,CommunicationService> onlineClients = new ConcurrentHashMap<>();
            CommunicationService communicationService = new CommunicationService(pw,br,onlineClients);
            communicationService.initialize();
            CommunicationService communicationService2 = new CommunicationService(pw,br,onlineClients);
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
            CommunicationService communicationService = new CommunicationService(pw,br,onlineClients);
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
}