package Client;

import Utils.Tuple;
import Server.TaggedConnection;
import Utils.Frame;
import Utils.Tag;
import View.IO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReplyHandler implements Runnable {
    TaggedConnection dI;
    InputHandler iH;
    IO o;

    public ReplyHandler(TaggedConnection dI, InputHandler iH, IO o) {
        this.dI = dI;
        this.iH = iH;
        this.o= o;
    }

    @Override
    public void run() {
        o.file("Welcome");
        try {
            Frame f;
            Tuple<Integer,Integer> t1;
            Tuple<Integer,Integer> t2;
            int n;
            while ((f=dI.receive()).tag!= Tag.QUIT) { // enquanto a tag da mensagem recebida não for quit continua a tentar receber mais respostas
                switch (f.tag) {
                    case HELP -> o.file(new String(f.data.get(0))); //vai ler o ficheiro com o nome enviado como argumento no Frame
                    case ERROR -> o.error(new String(f.data.get(0))); //apresenta o erro enviado no frame
                    case LOGIN -> o.welcome(new String(f.data.get(0)));
                    case GETMAPDATA -> {
                        int tamanho = Integer.parseInt(new String(f.data.get(0)));
                        Map<Tuple<Integer,Integer>,Tuple<Integer,Integer>> grelha=new HashMap<>();
                        for (int i=1;i<=4*tamanho;i+=4) {
                            t1 = new Tuple<>(Integer.parseInt(new String(f.data.get(i))), Integer.parseInt(new String(f.data.get(i+1))));
                            t2 = new Tuple<>(Integer.parseInt(new String(f.data.get(i+2))), Integer.parseInt(new String(f.data.get(i+3))));
                            grelha.put(t1,t2);
                        }
                        o.mapData(grelha); //apresenta os 2 tuplos de forma a perceber o que significam (primeiro é a posição o segundo é o número de utilizadores que estiveram nessa posição e o número de doentes também nessa posição
                    }
                    case NUMOFPEOPLEON -> {
                        t1 = new Tuple<>(Integer.parseInt(new String(f.data.get(0))), Integer.parseInt(new String(f.data.get(1))));
                        n = Integer.parseInt(new String(f.data.get(2)));
                        o.numOfPeopleOn(t1, n); //apresenta t1 como sendo o tuplo de posição e o n como sendo o número de pessoas que o servidor viu nesse local
                    }
                    case REMINDWHENEMPTY -> o.remindEmpty(f.data); //Apresenta a posição que recebeu como argumento como estando vazia, se receber nada é apenas um aviso
                    default -> o.info(f.tag);
                }
            }
            o.info(f.tag);
        }
        catch (IOException e) {
            o.error("Ocorreu um erro de Input/Output");
        }
    }
}
