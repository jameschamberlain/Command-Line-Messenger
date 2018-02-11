
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
      while (true) {
        String userInput = user.readLine();

        if (userInput.equals("quit")) {
          server.println(userInput);
          break;
        }

        String text = user.readLine();
        server.println(userInput); // Matches CCCCC in ServerReceiver
        server.println(text);      // Matches DDDDD in ServerReceiver
      }
    } catch (IOException e) {
      Report.errorAndGiveUp("Communication broke in ClientSender"
                        + e.getMessage());
    }

    Report.behaviour("Client sender thread ending"); // Matches GGGGG in Client.java
  }
}

/*

What happens if recipient is null? Then, according to the Java
documentation, println will send the string "null" (not the same as
null!). So maye we should check for that case! Paticularly in
extensions of this system.

 */
