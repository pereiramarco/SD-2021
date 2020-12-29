package Client;

import Server.TaggedConnection;
import View.IO;

import java.io.*;
import java.net.Socket;

public class Client {
    static Socket s;

    static DataInputStream dI;
    static DataOutputStream dO;

    static ReplyHandler rH;
    static InputHandler iH;

    static IO o;

    public static void main(String args[]) throws IOException, InterruptedException {
        s = new Socket("localhost", 12345);

        o = new IO(); // Classe responsável pela View
        TaggedConnection tC = new TaggedConnection(s);

        iH = new InputHandler(tC,o); //criação do handler para o input do user
        rH = new ReplyHandler(tC,iH,o); //criação do handler para as respostas do servidor

        //são threads independentes e devem correr em simultâneo

        Thread tI = new Thread(iH);
        Thread tR = new Thread(rH);

        tR.start();
        tI.start();

        tR.join();
        tI.join();

        s.shutdownInput();
        s.shutdownOutput();
        s.close();
    }
}
