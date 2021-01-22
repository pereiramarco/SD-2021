package Server;
import Model.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        Info info = new Info();
        info.addNewUser("VIP","123" ); //adiciona o utilizador para facilitar testes
        info.addNewUser("Joca","123" ); //adiciona o utilizador para facilitar testes
        info.addNewUser("Optimus","123" ); //adiciona o utilizador para facilitar testes
        info.addNewUser("Prime","123" ); //adiciona o utilizador para facilitar testes
        info.addVIP("VIP"); //adiciona VIP como um utilizador com acessos especiais

        while (true) {
            Socket socket = serverSocket.accept();
            Thread worker = new Thread(new ServerConnection(new TaggedConnection(socket), info));
            worker.start();
        }
    }
}
