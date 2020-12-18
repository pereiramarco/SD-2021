package Client;

import Utils.Colors;
import View.Output;

import java.io.DataInputStream;
import java.io.IOException;

public class ReplyHandler implements Runnable {
    DataInputStream dI;
    InputHandler iH;
    Output o;

    public ReplyHandler(DataInputStream dI,InputHandler iH,Output o) {
        this.dI = dI;
        this.iH = iH;
        this.o= o;
    }

    @Override
    public void run() {
        String reply;
        o.file("Welcome");
        try {
            while (!(reply = dI.readUTF()).equals(Colors.ANSI_GREEN +"Obrigado por usar CoronaBYErus")) {
                if (reply.equals("h1"))
                    o.file("MenuBig1");
                else
                    if (reply.equals("h2"))
                        o.file("MenuBig2");
                    else
                        if (reply.equals("h3"))
                            o.file("MenuSmall");
                        else
                            o.info(reply);
            }
            o.info(reply);
        }
        catch (IOException e) {
            o.error("Ocorreu um erro de Input/Output");
        }
    }
}
