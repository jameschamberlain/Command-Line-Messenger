
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;


// Repeatedly reads recipient's nickname and text from the user in two
// separate lines, sending them to the server (read by ServerReceiver
// thread).

public class ClientSender extends Thread {

    private String nickname;
    private PrintStream server;
    private boolean shouldBreak = false;

    ClientSender(String nickname, PrintStream server) {
        this.nickname = nickname;
        this.server = server;
    }

    /**
     * Start ClientSender thread.
     */
    public void run() {
        // So that we can use the method readLine:
        BufferedReader user = new BufferedReader(new InputStreamReader(System.in));

        try {
            // Then loop forever sending messages to recipients via the server:
            while (!shouldBreak) {
                String command = user.readLine();

                switch (command) {
                    case "logout":
                        server.println(command);
                        shouldBreak = true;
                        break;
                    case "send":
                        System.out.print("recipient: ");
                        String recipient = user.readLine();
                        System.out.print("message: ");
                        String message = user.readLine();
                        server.println(command);      // Matches YYYYY in ClientSender.java
                        server.println(recipient);    // Matches CCCCC in ServerReceiver
                        server.println(message);      // Matches DDDDD in ServerReceiver
                        System.out.println("Message sent");
                        break;
                    case "previous":
                        server.println(command);      // Matches YYYYY in ClientSender.java
                        break;
                    case "next":
                        server.println(command);      // Matches YYYYY in ClientSender.java
                        break;
                    case "delete":
                        server.println(command);      // Matches YYYYY in ClientSender.java
                        break;
                    case "current":
                        server.println(command);      // Matches YYYYY in ClientSender.java
                        break;
                    case "help":
                        System.out.println("Possible commands: [send, previous, next, delete, current, help]");
                        break;
                    default:
                        System.out.println("Command not recognised. For a list of available commands type \'help\'");
                        break;
                }




            }
        }
        catch (IOException e) {
            Report.errorAndGiveUp("Communication broke in ClientSender" + e.getMessage());
        }

        Report.behaviour("Client sender thread ending"); // Matches GGGGG in Client.java
    }
}

/*

What happens if recipient is null? Then, according to the Java
documentation, println will send the string "null" (not the same as
null!). So maybe we should check for that case! Particularly in
extensions of this system.

 */
