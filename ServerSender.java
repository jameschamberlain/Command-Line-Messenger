import java.io.PrintStream;
import java.util.concurrent.CopyOnWriteArrayList;

// Continuously reads from message list for a particular client,
// forwarding to the client. Also performs commands from the user

public class ServerSender extends Thread {

    /**
     * Stores the global client table
     */
    private CopyOnWriteArrayList<Message> clientMessages;
    /**
     * Stores the communication stream to the client
     */
    private PrintStream client;
    /**
     * Stores the size of the client messages list before operations occur
     */
    private int oldSize = 0;
    /**
     * Stores the pointer for the current message
     */
    private int currentMessage;
    /**
     * Stores the message before operations occur
     */
    private Message oldMessage;
    /**
     * Stores whether the last command a client performed was 'delete'
     */
    private boolean hasDeletedMessage = false;


    /**
     * Constructs a new server sender
     *
     * @param l messages from this queue will be sent to the client
     * @param c the stream used to send data to the client
     */
    public ServerSender(CopyOnWriteArrayList<Message> l, PrintStream c) {
        this.clientMessages = l;
        this.client = c;
    }

    /**
     * Starts this server sender.
     */
    public void run() {

        // Set the current message to the newest message
        if (clientMessages.size() > 1) {
            currentMessage = (clientMessages.size() - 1);
        }
        else {
            currentMessage = 0;
        }

        // When the client has a new message print it
        while (true) {
            if (clientMessages.size() > 0 && oldSize < clientMessages.size()) {
                Message msg = clientMessages.get(clientMessages.size() - 1); // Matches EEEEE in ServerReceiver
                client.println(msg); // Matches FFFFF in ClientReceiver
                oldSize = clientMessages.size();
                currentMessage = clientMessages.size() - 1;
            }
        }

    }
    // Throws InterruptedException if interrupted while waiting


    /**
     * Prints the previous message
     */
    public void previous() {
        if (currentMessage > 0) {
            Message msg = clientMessages.get(currentMessage - 1);
            client.println(msg); // Matches FFFFF in ClientReceiver
            currentMessage -= 1;
            oldMessage = clientMessages.get(currentMessage);
        }
        else {
            client.println("No previous message"); // Matches FFFFF in ClientReceiver
        }
    }

    /**
     * Prints the next message
     */
    public void next() {
        if (currentMessage < (clientMessages.size() - 1)) {
            Message msg = clientMessages.get(currentMessage + 1);
            client.println(msg); // Matches FFFFF in ClientReceiver
            currentMessage += 1;
            oldMessage = clientMessages.get(currentMessage);
        }
        else {
            client.println("No next message"); // Matches FFFFF in ClientReceiver
        }
    }

    /**
     * Deletes the current message
     */
    public void deleteMessage() {
        if (clientMessages.size() > 0) {
            clientMessages.remove(currentMessage);
            client.println("Message deleted"); // Matches FFFFF in ClientReceiver
            if (currentMessage >= clientMessages.size()) {
                currentMessage -= 1;
                oldMessage = clientMessages.get(currentMessage);
                hasDeletedMessage = true;
            }
        }
        else {
            client.println("No message to delete"); // Matches FFFFF in ClientReceiver
        }
    }

    /**
     * Prints the current message
     */
    public void currentMessage() {
        if (clientMessages.size() > 0) {
            if (currentMessage >= clientMessages.size()) {
                currentMessage -= 1;
            }
            else if ((clientMessages.get(currentMessage) != oldMessage) && (!hasDeletedMessage) && (currentMessage > 0)) {
                currentMessage -= 1;
            }
            Message msg = clientMessages.get(currentMessage);
            client.println(msg); // Matches FFFFF in ClientReceiver
            hasDeletedMessage = false;
        }
        else {
            client.println("No current message"); // Matches FFFFF in ClientReceiver
        }
    }

}
