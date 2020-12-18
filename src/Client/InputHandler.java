package Client;

import View.Output;

import java.io.DataOutputStream;
import java.io.IOException;

public class InputHandler implements Runnable{
    DataOutputStream dO;
    Output o;

    boolean exit=false;

    public InputHandler(DataOutputStream dO,Output o) {
        this.dO = dO;
        this.o = o;
    }

    @Override
    public void run() {
        String command;
        try {
            while (!exit && (command = o.read()) != null) {
                switch (command) {
                    case "infected":
                    case "quit":
                        exit=true;
                    default:
                        dO.writeUTF(command);
                        dO.flush();
                }
            }
        }
        catch (IOException e)  {
            o.error("Ocorreu um erro de Input/Output");
        }
    }
}
