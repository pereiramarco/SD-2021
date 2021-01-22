package Server;

import Utils.*;

import java.io.IOException;
import java.util.ArrayList;

public class NotifyInfectedContact implements Runnable{
    TaggedConnection tC;

    public NotifyInfectedContact(TaggedConnection tCG) {
        tC = tCG;
    }

    @Override
    public void run() {
        try {
            tC.send(new Frame(Tag.DANGER,null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
