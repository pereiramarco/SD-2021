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
        Tag tag;
        try {
            while (!exit && (c = o.read()) != null) {
                command=c.split(" ");
                List<byte[]> data=new ArrayList<>();
                switch (command[0]) {
                    case "quit":
                        exit = true; // se este comando for chamado o servidor deverá fechar a conexão com este lado desta forma garantimos que o ciclo termian na próxima iteração
                        break;
                    case "infected":
                    case "logout":
                    case "help":
                    case "getMapData":
                    case "makeImportant":
                        break;
                    case "login": // qualquer um destes comandos até ao default tem 2 argumentos logo faz-se o mesmo para todos
                    case "signup":
                    case "changePos":
                    case "numOfPeopleOn":
                    case "remindWhenEmpty":
                        if (command.length < 3) {
                            o.error("Número de dados inseridos incorreto");
                            continue;
                        }
                        data.add(command[1].getBytes());
                        data.add(command[2].getBytes());
                        break;
                    default:
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
