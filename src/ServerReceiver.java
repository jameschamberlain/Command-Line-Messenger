
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

// Gets messages from client and puts them in a queue, for another
// thread to forward to the appropriate client.

public class ServerReceiver extends Thread {
    private String myClientsName;
    private BufferedReader myClient;
    private ClientTable clientTable;
    private ServerSender companion;
    private boolean shouldBreak = false;

    /**
     * Constructs a new server receiver.
     *
     * @param n the name of the client with which this server is communicating
     * @param c the reader with which this receiver will read data
     * @param t the table of known clients and connections
     * @param s the corresponding sender for this receiver
     */
    public ServerReceiver(String n, BufferedReader c, ClientTable t, ServerSender s) {
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
            while (!shouldBreak) {
                String command = myClient.readLine();    // Matches YYYYY in ClientSender.java

                switch (command) {
                    case "logout":
                        shouldBreak = true;
                        break;
                    case "send":
                        String recipient = myClient.readLine();    // Matches CCCCC in ClientSender.java
                        String message = myClient.readLine();      // Matches DDDDD in ClientSender.java

                        if (message != null) {
                            Message msg = new Message(myClientsName, message);
                            CopyOnWriteArrayList<Message> recipientsMessages = clientTable.getMessages(recipient); // Matches EEEEE in ServerSender.java
                            if (recipientsMessages != null) {
                                recipientsMessages.add(msg);
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

