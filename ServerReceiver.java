import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

// Gets messages from client and puts them in a list, for another
// thread to forward to the appropriate client. It also forwards
// commands to the server

public class ServerReceiver extends Thread {

    /**
     * Stores the name of the client
     */
    private String myClientsName;
    /**
     * Stores the communication stream to the client
     */
    private BufferedReader myClient;
    /**
     * Stores the global client table
     */
    private ClientTable clientTable;
    /**
     * Stores the companion thread to be able to communicate with the server and other clients
     */
    private ServerSender companion;
    /**
     * Stores whether the thread should stop running or not
     */
    private boolean shouldBreak = false;
    /**
     * Temporarily stores the messages of the recipient
     */
    private ArrayList<String> userMessagesTemp;

    /**
     * Stores the path to the folder containing the users' messages
     */
    private static File userMessagesFile;
    /**
     * Stores the file path as a string
     */
    private static String userMessagesPath;


    /**
     * Constructs a new server receiver
     *
     * @param n The name of the client with which this server is communicating
     * @param c The reader with which this receiver will read data
     * @param t The table of known clients and connections
     * @param s The corresponding sender for this receiver
     */
    ServerReceiver(String n, BufferedReader c, ClientTable t, ServerSender s) {
        this.myClientsName = n;
        this.myClient = c;
        this.clientTable = t;
        this.companion = s;
    }

    /**
     * Starts this server receiver.
     */
    public void run() {
        try {
            // Loop until the client logs out,
            // or the end of the stream is reached
            // sending messages to recipients
            // and commands to the server via the server:
            while (!shouldBreak) {

                String command = myClient.readLine();    // Matches YYYYY in ClientSender.java

                switch (command) {
                    case "logout":
                        shouldBreak = true;
                        break;
                    case "send":
                        String recipient = myClient.readLine();    // Matches CCCCC in ClientSender.java
                        String message = myClient.readLine();      // Matches DDDDD in ClientSender.java

                        // If the clients message is not empty then add it to the client table and the recipients messages file.
                        if (message != null) {
                            Message msg = new Message(myClientsName, message);
                            CopyOnWriteArrayList<Message> recipientsMessages = clientTable.getMessages(recipient); // Matches EEEEE in ServerSender.java

                            if (recipientsMessages != null) {
                                recipientsMessages.add(msg);
                                userMessagesFile = new File("resources/user_messages/" + recipient + ".txt");
                                userMessagesPath = userMessagesFile.getAbsolutePath();
                                userMessagesTemp = new ArrayList<>();
                                String s;
                                for (Message m : recipientsMessages) {
                                    s = m.toString();
                                    userMessagesTemp.add(s);
                                }
                                Server.writeFile(userMessagesFile, userMessagesPath, userMessagesTemp);
                            }
                            else {
                                Report.error("Message for nonexistent client " + recipient + ": " + message);
                            }
                        }
                        else {
                            // No point in closing socket. Just give up.
                            return;
                        }
                        break;
                    case "previous":
                        companion.previous();
                        break;
                    case "next":
                        companion.next();
                        break;
                    case "delete":
                        companion.deleteMessage();
                        break;
                    case "current":
                        companion.currentMessage();
                        break;
                    case "help":
                        break;
                    default:
                        // May be end of stream reached
                        shouldBreak = true;
                        break;
                }

            }
        }
        catch (IOException e) {
            Report.error("Something went wrong with the client " + myClientsName + " " + e.getMessage());
            // No point in trying to close sockets. Just give up.
            // We end this thread (we don't do System.exit(1)).
        }

        companion.interrupt();
        Report.behaviour(myClientsName + " disconnected");
        Server.usersOnline.remove(myClientsName);
        Server.print("Current users online", Server.usersOnline);
    }
}

