package Client;

import Exceptions.NotRecognizedException;
import Server.TaggedConnection;
import Utils.Frame;
import Utils.Tag;
import View.IO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InputHandler implements Runnable{
    TaggedConnection dO;
    IO o;

    boolean exit=false;

    public InputHandler(TaggedConnection dO, IO o) {
        this.dO = dO;
        this.o = o;
    }

    @Override
    public void run() {
        String c;
        String[] command;
        int indexOfCommand;
        List<String> options = List.of("quit","infected","logout","help","getMapData","makeImportant",
                "login","signup","changePos","numOfPeopleOn","remindWhenEmpty");
        Tag tag;
        try {
            while (!exit && (c = o.read()) != null) {
                command=c.split(" ");
                List<byte[]> data=new ArrayList<>();
                indexOfCommand = options.indexOf(command[0]);
                if(indexOfCommand == 0)
                    exit = true;
                else if(indexOfCommand > 5) {
                    if (command.length != 3) {
                        o.error("Número de dados inseridos incorreto");
                        continue;
                    }
                    else {
                        data.add(command[1].getBytes());
                        data.add(command[2].getBytes());
                    }
                }
                else if(indexOfCommand == -1) {
                    o.error("Comando não reconhecido tente help");
                    continue;
                }
                tag = Tag.valueOf(command[0].toUpperCase()); // tag é um enum com os nomes exatamente iguais aos comandos para tornar mais fácil a conversão
                Frame f = new Frame(tag,data);
                dO.send(f);
            }
        }
        catch (IOException e)  {
            o.error("Ocorreu um erro de Input/Output");
        }
    }
}
