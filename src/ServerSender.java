
import java.io.PrintStream;
import java.util.concurrent.CopyOnWriteArrayList;

// Continuously reads from message queue for a particular client,
// forwarding to the client.

public class ServerSender extends Thread {
    private CopyOnWriteArrayList<Message> clientMessages;
    private PrintStream client;
    private int oldSize = 0;
    private int currentMessage;
    private Message oldMessage;
    private boolean hasDeletedMessage = false;

    /**
     * Constructs a new server sender.
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

        if (clientMessages.size() > 1) {
            currentMessage = (clientMessages.size() - 1);
        }
        else {
            currentMessage = 0;
        }


        while (true) {
            if (clientMessages.size() > 0 && oldSize < clientMessages.size()) {
                Message msg = clientMessages.get(clientMessages.size() - 1); // Matches EEEEE in ServerReceiver
                client.println(msg); // Matches FFFFF in ClientReceiver
                oldSize = clientMessages.size();
                currentMessage = clientMessages.size() - 1;
            }
        }

    }
    /*

     * Throws InterruptedException if interrupted while waiting

     * See https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/BlockingQueue.html#take--

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
