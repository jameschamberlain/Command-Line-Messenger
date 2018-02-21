// Each nickname has a different incoming-message queue.

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientTable {

    /**
     * Stores the messages of every user
     */
    private ConcurrentMap<String, CopyOnWriteArrayList<Message>> allUsersMessages = new ConcurrentHashMap<>();

    /**
     *
     * Adds the client and their messages to the global map for users
     *
     * @param nickname The nickname of the client
     * @param list The list of messages for the client
     */
    public void add(String nickname, CopyOnWriteArrayList<Message> list) {
        allUsersMessages.put(nickname, list);
    }

    /**
     *
     * Gets the messages for the selected user
     * Returns null if the nickname is not in the table
     *
     * @param nickname
     * @return
     */
    public CopyOnWriteArrayList<Message> getMessages(String nickname) {
        return allUsersMessages.get(nickname);
    }

    /**
     *
     * Removes the user and their messages from the global map for users
     *
     * @param nickname The nickname for the client
     */
    public void remove(String nickname) {
        allUsersMessages.remove(nickname);
    }

}
