// Usage:
//        java Server
//
// There is no provision for ending the server gracefully.  It will
// end if (and only if) something exceptional happens.

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Server {


    private static ArrayList<String> users = new ArrayList<>();
    static ArrayList<String> usersOnline = new ArrayList<>();
    private static File usersFile = new File("src/users/users.txt");
    private static String usersPath = usersFile.getAbsolutePath();
    private static boolean isLoggedIn = false;

    /**
     * Start the server listening for connections.
     */
    public static void main(String[] args) {

        readUsernamesFile();

        print("Users registered", users);

        // This table will be shared by the server threads:
        ClientTable clientTable = new ClientTable();

        /**
         * Add previously registered users to the ClientTable so messages can be stored
         */
        for (String user : users) {
            clientTable.add(user);
        }

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(Port.number);
        }
        catch (IOException e) {
            Report.errorAndGiveUp("Couldn't listen on port " + Port.number);
        }

        try {
            // We loop for ever, as servers usually do.
            while (true) {
                // Listen to the socket, accepting connections from new clients:
                Socket socket = serverSocket.accept(); // Matches AAAAA in Client

                // This is so that we can send messages to the client:
                PrintStream toClient = new PrintStream(socket.getOutputStream());
                // This is so that we can use readLine():
                BufferedReader fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // While the client isn't logged in continue to talk to them directly
                while (!isLoggedIn) {
                    // We ask the client what its name is and whether they are a new user:
                    String clientName = fromClient.readLine(); // Matches BBBBB in Client
                    String newUser = fromClient.readLine(); // Matches ZZZZZ in Client

                    if (newUser.equals("true")) {
                        if (users.contains(clientName)) {
                            toClient.println("Username already taken, please select another or login as that user");
                        }
                        else {
                            isLoggedIn = true;
                            toClient.println("Register and login successful");
                            Report.behaviour("New user: " + clientName + " connected");
                            // We add the new client to the table:
                            clientTable.add(clientName);
                            // We add the new client to the list of users and online users
                            users.add(clientName);
                            usersOnline.add(clientName);
                            writeUsernamesFile();
                            print("Current users online", usersOnline);

                            // We create and start a new thread to write to the client:
                            ServerSender serverSender = new ServerSender(clientTable.getMessages(clientName), toClient);
                            serverSender.start();

                            // We create and start a new thread to read from the client:
                            (new ServerReceiver(clientName, fromClient, clientTable, serverSender)).start();
                        }
                    }
                    else if (newUser.equals("false")) {
                        if (users.contains(clientName)) {
                            toClient.println("Login successful");

                            isLoggedIn = true;

                            Report.behaviour(clientName + " connected");

                            // We add the client to the list of online users
                            usersOnline.add(clientName);
                            print("Current users online", usersOnline);

                            // We create and start a new thread to write to the client:
                            ServerSender serverSender = new ServerSender(clientTable.getMessages(clientName), toClient);
                            serverSender.start();

                            // We create and start a new thread to read from the client:
                            (new ServerReceiver(clientName, fromClient, clientTable, serverSender)).start();

                        }
                        else {
                            toClient.println("Username not recognised");
                        }

                    }
                }
                isLoggedIn = false;
            }
        }
        catch (IOException e) {
            // Lazy approach:
            Report.error("IO error " + e.getMessage());
            // A more sophisticated approach could try to establish a new
            // connection. But this is beyond the scope of this simple exercise.
        }
    }


    /**
     * Takes the users file and stores the usernames
     */
    private static void readUsernamesFile() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(usersPath));
        }
        catch (FileNotFoundException e) {
            Report.error("Couldn't find \"users.txt\" file");
            Report.behaviour("    Creating the \"users.txt\" file...");
            try {
                usersFile.createNewFile();
                readUsernamesFile();
            }
            catch (IOException e2) {
                Report.error("Couldn't create \"users.txt\" file");
            }
            Report.behaviour("    Created \"users.txt\" file");
        }
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String user = line;
                users.add(user);
            }
        }
        catch (NullPointerException | IOException e) {
            Report.error("Couldn't read \"users.txt\" file\n    The file may be empty");
        }
    }


    /**
     * Writes the usernames to a file
     */
    private static void writeUsernamesFile() {
        //String userNames = "";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(usersPath), false));
            for (String user : users) {
                //userNames += user + "\n";
                writer.write(user);
                writer.newLine();
            }
            writer.close();
            //List<String> lines = Arrays.asList(userNames);
            //Path file = Paths.get(usersPath);
            //Files.write(file, lines, Charset.forName("UTF-8"));
        }
        catch (IOException e) {
            Report.error(e.getMessage());
        }
    }


    /**
     *
     * @param newArrayList ArrayList to be printed to the screen
     */
    public static void print(String description, ArrayList newArrayList) {
        String arrayText = description + ": [";
        if (newArrayList.size() == 0) {
            for (Object i : newArrayList) {
                arrayText += i;
            }
        }
        else {
            arrayText += newArrayList.get(0);
            for (int i = 1; i < newArrayList.size(); i++) {
                arrayText += ", " + newArrayList.get(i);
            }
        }
        arrayText += "]";
        Report.behaviour(arrayText);
    }

}
