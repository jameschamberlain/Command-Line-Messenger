// Usage:
//        java Server
//
// There is no provision for ending the server gracefully.  It will
// end if (and only if) something exceptional happens.

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    /**
     * Stores the list of users
     */
    private static ArrayList<String> users = new ArrayList<>();
    /**
     * Stores the list of users who are currently online
     */
    static ArrayList<String> usersOnline = new ArrayList<>();
    /**
     * Stores the path to the file containing the list of users
     */
    private static File usersFile = new File("src/resources/users.txt");
    /**
     * Stores the file path as a string
     */
    private static String usersPath = usersFile.getAbsolutePath();
    /**
     * Stores the path to the folder containing the users' messages
     */
    private static File userMessagesFile;
    /**
     * Stores the file path as a string
     */
    private static String userMessagesPath;
    /**
     * Stores whether the user is logged in or not
     */
    private static boolean isLoggedIn = false;
    /**
     * Stores the messages of the recipient
     */
    private static CopyOnWriteArrayList<Message> userMessages;
    /**
     * Temporarily stores the messages of the recipient
     */
    private static ArrayList<String> userMessagesTemp;


    /**
     * Start the server listening for connections.
     */
    public static void main(String[] args) {

        readFile(usersFile, usersPath, users);

        print("Users registered", users);

        // This table will be shared by the server threads:
        ClientTable clientTable = new ClientTable();

        // Add previously registered users, and their messages, to the client table
        for (String user : users) {
            userMessagesFile = new File("src/resources/user_messages/" + user + ".txt");
            userMessagesPath = userMessagesFile.getAbsolutePath();
            userMessagesTemp = new ArrayList<>();
            readFile(userMessagesFile, userMessagesPath, userMessagesTemp);
            userMessages = new CopyOnWriteArrayList<>();
            for (String s : userMessagesTemp) {
                userMessages.add(Message.toMessage(s));
            }
            clientTable.add(user, userMessages);
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

                    // Steps through the register/login process with the client
                    if (newUser.equals("true")) {
                        if (users.contains(clientName)) {
                            toClient.println("Username already taken, please select another or login as that user");
                        }
                        else {
                            isLoggedIn = true;
                            toClient.println("Register and login successful");
                            Report.behaviour("New user: " + clientName + " connected");
                            // We add the new client to the table:
                            userMessages = new CopyOnWriteArrayList<>();
                            clientTable.add(clientName, userMessages);
                            // We add the new client to the list of users and online users
                            users.add(clientName);
                            usersOnline.add(clientName);
                            writeFile(usersFile, usersPath, users);
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
                // If the login/register process is not complete keep the while loop going
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
     *
     * Takes a file and stores its contents in a list
     *
     * @param file The file to be read
     * @param path The path to the specified file
     * @param list The list that the file contents should be stored in
     */
    private static void readFile(File file, String path, ArrayList<String> list) {
        // Create a new BufferedReader that will go onto read the file's contents
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(path));
        }
        catch (FileNotFoundException e) {
            Report.error("Couldn't find file");
            Report.behaviour("    Creating the file...");
            try {
                // If the file does not exist, try and create the file
                file.getParentFile().mkdirs();
                file.createNewFile();
                readFile(file, path, list);
            }
            catch (IOException e2) {
                Report.error("Couldn't create file");
            }
            Report.behaviour("    Created file");
        }

        // Stores the current line in the file
        String line;

        try {
            // While the current line is not empty, read it and store it in the list
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
            reader.close();
        }
        catch (NullPointerException | IOException e) {
            Report.error("Couldn't read file\n    The file may be empty");
        }
    }


    /**
     *
     * Write a list to a file
     *
     * @param path The path for the new file
     * @param list The list which holds the content
     */
    static void writeFile(File file, String path, ArrayList<String> list) {
        try {
            // Create a new BufferedWrite to write to the file
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path), false));
            // For every item in the list, write it to the file and then add a line break
            for (String item : list) {
                writer.write(item);
                writer.newLine();
            }
            writer.close();
        }
        catch (FileNotFoundException f) {
            readFile(file, path, list);
            writeFile(file, path, list);
        }
        catch (IOException e) {
            Report.error(e.getMessage());
        }
    }


    /**
     *
     * Prints a list along with a description
     * in a nice format:
     * Description: [1, 2, 3]
     *
     * @param description The accompanying description
     * @param newArrayList List to be printed to the screen
     */
    public static void print(String description, ArrayList newArrayList) {
        String arrayText = description + ": [";
        if (newArrayList.size() > 0) {
            arrayText += newArrayList.get(0);
            for (int i = 1; i < newArrayList.size(); i++) {
                arrayText += ", " + newArrayList.get(i);
            }
        }
        arrayText += "]";
        Report.behaviour(arrayText);
    }

}
