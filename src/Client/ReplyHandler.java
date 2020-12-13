package Client;

import java.io.DataInputStream;
import java.io.IOException;

public class ReplyHandler implements Runnable {
    DataInputStream dI;
    InputHandler iH;

    public ReplyHandler(DataInputStream dI,InputHandler iH) {
        this.dI = dI;
        this.iH = iH;
    }

    @Override
    public void run() {
        String reply;
        System.out.println("USA help PARA SABER O QUE PODES FAZER");
        try {
            while (!(reply = dI.readUTF()).equals("quit") && !reply.equals("BONITO SERVIÇO PANELEIRO")) {
                System.out.println(reply);
            }
            System.out.println(reply);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
