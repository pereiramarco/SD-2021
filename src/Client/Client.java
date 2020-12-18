package Client;

import View.Output;

import java.io.*;
import java.net.Socket;

public class Client {
    static Socket s;

    static DataInputStream dI;
    static DataOutputStream dO;

    static ReplyHandler rH;
    static InputHandler iH;

    static Output o;

    public static void main(String args[]) throws IOException, InterruptedException {
        s = new Socket("localhost", 12345);
        dI = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        dO = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));

        o = new Output();

        iH = new InputHandler(dO,o);
        rH = new ReplyHandler(dI,iH,o);

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
