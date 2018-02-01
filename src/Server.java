import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements GameConstraints {
    private static final int port = 1235;
    public static void main(String[] args) throws Exception {
        System.out.println("\n\t\t...Serwer zostal wlaczony...");

        ServerSocket serverSocket = new ServerSocket(ServerConfiguration.getPort());
        int sessionNumber = 1;

        try {
            while(true) {
                Socket player1 = serverSocket.accept();
                new DataOutputStream(player1.getOutputStream()).writeInt(PLAYER1);

                Socket player2 = serverSocket.accept();
                new DataOutputStream(player2.getOutputStream()).writeInt(PLAYER2);

                Connection connection = new Connection(player1, player2, sessionNumber);
                new Thread(connection).start();
                sessionNumber++;
            }
        } catch(Exception e) {}
        serverSocket.close();
    }
}
