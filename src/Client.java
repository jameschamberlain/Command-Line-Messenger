// Usage:
//        java Client user-nickname server-hostname
//
// After initializing and opening appropriate sockets, we start two
// client threads, one to send messages, and another one to get
// messages.
//
// A limitation of our implementation is that there is no provision
// for a client to end after we start it. However, we implemented
// things so that pressing ctrl-c will cause the client to end
// gracefully without causing the server to fail.
//
// Another limitation is that there is no provision to terminate when
// the server dies.

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

class Client {

    private static String command;
    private static String nickname;
    private static boolean isNewUser;
    private static boolean isLoggedIn = false;
    private static Scanner userInput;

    public static void main(String[] args) {

        // Check correct usage:
        if (args.length != 1) {
            Report.errorAndGiveUp("Usage: java Client server-hostname");
        }

        // Initialize information:
        String hostname = args[0];

        userInput = new Scanner(System.in);

        signInProcess();


        // Open sockets:
        PrintStream toServer = null;
        BufferedReader fromServer = null;
        Socket server = null;

        try {
            server = new Socket(hostname, Port.number); // Matches AAAAA in Server.java
            toServer = new PrintStream(server.getOutputStream());
            fromServer = new BufferedReader(new InputStreamReader(server.getInputStream()));
        }
        catch (UnknownHostException e) {
            Report.errorAndGiveUp("Unknown host: " + hostname);
        }
        catch (IOException e) {
            Report.errorAndGiveUp("The server doesn't seem to be running " + e.getMessage());
        }

        while (!isLoggedIn) {
            // Tell the server what my nickname is:
            toServer.println(nickname + "\n" + isNewUser); // Matches BBBBB and ZZZZZ in Server.java

            try {

                String resultFromServer = fromServer.readLine(); // Matches ZZZZZ in Client

                switch (resultFromServer) {
                    case "Username already taken, please select another":
                        System.out.println(resultFromServer);
                        signInProcess();
                        break;
                    case "Register and login successful":
                        System.out.println(resultFromServer);
                        isLoggedIn = true;
                        break;
                    case "Login successful":
                        System.out.println(resultFromServer);
                        isLoggedIn = true;
                        break;
                    case "Username not recognised":
                        System.out.println(resultFromServer);
                        signInProcess();
                        break;
                    default:
                        System.out.println("Could not successfully communicate with server, please try again");
                        signInProcess();
                }

            }
            catch (IOException e) {
                Report.error("IO error " + e.getMessage());
            }
        }


        // Create two client threads of a different nature:
        ClientSender sender = new ClientSender(nickname, toServer);
        ClientReceiver receiver = new ClientReceiver(fromServer);

        // Run them in parallel:
        sender.start();
        receiver.start();

        // Wait for them to end and close sockets.
        try {
            sender.join();         // Waits for ClientSender.java to end. Matches GGGGG.
            Report.behaviour("Client sender ended");
            toServer.close();      // Will trigger SocketException
            fromServer.close();    // (matches HHHHH in ClientServer.java).
            server.close();        // https://docs.oracle.com/javase/7/docs/api/java/net/Socket.html#close()
            receiver.join();
            Report.behaviour("Client receiver ended");
        }
        catch (IOException e) {
            Report.errorAndGiveUp("Something wrong " + e.getMessage());
        }
        catch (InterruptedException e) {
            Report.errorAndGiveUp("Unexpected interruption " + e.getMessage());
        }
        Report.behaviour("Client ended. Goodbye.");
        System.out.println("Logged out");
    }

    /**
     * Facilitates the initial command interface to register or login
     */
    private static void signInProcess() {
        System.out.println("Please enter either the \"register\" or \"login\" command and then your username");
        command = userInput.nextLine();

        switch (command) {
            case "register":
                isNewUser = true;
                nickname = userInput.nextLine();
                break;
            case "login":
                isNewUser = false;
                nickname = userInput.nextLine();
                break;
            default:
                System.out.println("Command not recognised, please try again");
                signInProcess();
        }

    }
}

