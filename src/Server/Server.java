package Server;
import Model.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        Info info = new Info();

        while (true) {
            Socket socket = serverSocket.accept();
            Thread worker = new Thread(new ServerConnection(socket, info));
            worker.start();
        }
    }
}
