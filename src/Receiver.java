import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
public class Receiver implements Runnable {
    private Socket socket;

    public Receiver(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("[Receiver] Received: " + message);
            }
        } catch (IOException e) {
            System.err.println("[Receiver Error] " + e.getMessage());
        } finally { // to ensure the socket is closed even if an error occurs
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("[Receiver Error] Failed to close socket: " + e.getMessage());
            }
        }
    }

}
