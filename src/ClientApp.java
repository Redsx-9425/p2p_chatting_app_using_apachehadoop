import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String username = sc.nextLine();
        String ip = "localhost";
        int port = (int) (Math.random() * (65535 - 49152 + 1)) + 49152; // random port between 49152 and 65535

        // listener for incoming msg
        PeerConnection peerConnection = new PeerConnection(port);
        Thread t = new Thread(peerConnection);
        t.setDaemon(true);
        t.start();

        HadoopRegistry.registerUser(username, ip, port);



        int choice = 0;
        do {
            System.out.println("\n1. List all users");
            System.out.println("2. Lookup user");
            System.out.println("3. Send message to user");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            if (sc.hasNextInt()) {// make sure the input is an integer
                choice = sc.nextInt();
                sc.nextLine(); // Clear buffer
            } else {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine(); // Clear invalid input
                continue;
            }
            switch (choice) {
                case 1:
                    System.out.println("\nActive Users:");
                    HadoopRegistry.listAllUsers().forEach(System.out::println);
                    break;
                case 2:
                    System.out.print("Enter username to lookup: ");
                    String lookupUser = sc.nextLine();
                    String userIpPort = HadoopRegistry.lookupUser(lookupUser);
                    if (userIpPort != null) {
                        System.out.println("User found: " + lookupUser + " at " + userIpPort);
                    }
                    break;
                case 3:
                    System.out.print("Enter destination username: ");
                    String dest = sc.nextLine().trim();
                    String destInfo = HadoopRegistry.lookupUser(dest);
                    if (destInfo != null) {
                        String[] parts = destInfo.split(":");
                        String desIp = parts[0];
                        int desPort = Integer.parseInt(parts[1]);
                        sendMessages(username, desIp, desPort, sc);
                    } else {
                        System.out.println("Destination '" + dest + "' not found.");
                    }
                    break;
                case 4:
                    System.out.println("Exiting...");
                    HadoopRegistry.deregisterUser(username);
                    t.interrupt();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again 1-4.");
            }
        } while (choice != 4);
    }

    private static void sendMessages(String myUsername, String ip, int port, Scanner sc) {
        try (
                Socket socket = new Socket(ip, port);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("\nChat start: ");
            System.out.println("Type your msg or 'exit' to end the chat.");
            while (true) {

                String msg = sc.nextLine();
                if (msg.equalsIgnoreCase("exit")) {
                    break;
                }
                if (msg.isBlank())
                    continue;

                writer.println(myUsername + ": " + msg);
            }

            writer.close();
            socket.close();
            System.out.println("Chat ended");
        } catch (IOException e) {
            System.err.println("[ClientApp Error] Failed to send message: " + e.getMessage());
        }
    }
}
