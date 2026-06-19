import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerConnection implements Runnable {
    private int port;


    public PeerConnection(int port){
        this.port = port;
    }

    @Override
    public void run() {
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new Receiver(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("[PeerConnection Error] " + e.getMessage());
        }
    }
}
