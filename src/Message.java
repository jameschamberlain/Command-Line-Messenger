public class Message {

    /**
     * Stores the sender of the message
     */
    private final String sender;
    /**
     * Stores the contents of the message
     */
    private final String text;


    /**
     *
     * Constructs a new message
     *
     * @param sender The sender of the message
     * @param text The content of the message
     */
    Message(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    /**
     *
     * Gets the sender of the message
     *
     * @return A string containing the sender of the message
     */
    public String getSender() {
        return sender;
    }

    /**
     *
     * Gets the contents of the message
     *
     * @return A string containing the contents of the message
     */
    public String getText() {
        return text;
    }

    /**
     *
     * Converts a message to a string
     *
     * @return A string containing the message
     */
    public String toString() {
        return "From " + sender + ": " + text;
    }

    /**
     *
     * Converts a string to a message
     *
     * @param newMessage The string to convert
     * @return A message containing the sender and the message
     */
    public static Message toMessage(String newMessage) {
        int space = newMessage.indexOf(" ");
        int colon = newMessage.indexOf(':');
        String newSender = newMessage.substring((space + 1), (colon));
        String newText = newMessage.substring((colon + 2));
        Message msg = new Message(newSender, newText);
        return msg;
    }
    
}
