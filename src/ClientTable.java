// Each nickname has a different incoming-message queue.

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientTable {

    private ConcurrentMap<String, CopyOnWriteArrayList<Message>> allUsersMessages = new ConcurrentHashMap<>();

    // The following overrides any previously existing nickname, and
    // hence the last client to use this nickname will get the messages
    // for that nickname, and the previously existing clients with that
    // nickname won't be able to get messages. Obviously, this is not a
    // good design of a messaging system. So I don't get full marks:

    public void add(String nickname) {
        allUsersMessages.put(nickname, new CopyOnWriteArrayList<>());
    }

    // Returns null if the nickname is not in the table:
    public CopyOnWriteArrayList<Message> getMessages(String nickname) {
        return allUsersMessages.get(nickname);
    }

    // Removes from table:
    public void remove(String nickname) {
        allUsersMessages.remove(nickname);
    }

}
