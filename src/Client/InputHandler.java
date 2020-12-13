package Client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;

public class InputHandler implements Runnable{
    DataOutputStream dO;
    BufferedReader userIn;

    boolean exit=false;

    public InputHandler(DataOutputStream dO,BufferedReader userIn) {
        this.dO = dO;
        this.userIn = userIn;
    }

    //retorna true enquanto o utilizador estiver dentro do programa
    public boolean isIn() {
        return !exit;
    }

    @Override
    public void run() {
        String command;
        try {
            while (!exit && (command = userIn.readLine()) != null) {
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
            e.printStackTrace();
        }
    }
}
