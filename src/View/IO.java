package View;

import Utils.Tuple;
import Utils.Colors;
import Utils.Tag;

import java.io.*;
import java.util.List;
import java.util.Map;

public class IO {
    BufferedReader userIn;

    public IO() {
        userIn=new BufferedReader(new InputStreamReader(System.in));
    }

    public String read() throws IOException {
        return userIn.readLine();
    }

    public void error(String s) {
        System.out.println(Colors.ANSI_RED + s + Colors.ANSI_RESET);
    }

    public void info(Tag tag) {
        switch (tag) {
            case QUIT:
                System.out.println(Colors.ANSI_GREEN + "Obrigado por usar CoronaBYErus"+ Colors.ANSI_RESET);
                break;
            case SIGNUP:
                System.out.println(Colors.ANSI_GREEN + "Seja Bem-vindo ao CoronaBYErus, a sua conta foi criada com sucesso use help para saber que comandos pode utilizar"+ Colors.ANSI_RESET);
                break;
            case LOGOUT:
                System.out.println("A sua sessão foi fechada, não se preocupe as suas notificações irão ser guardadas");
                break;
            case INFECTED:
                System.out.println("Fique em casa e tome cuidado");
                break;
            case CHANGEPOS:
                System.out.println("A sua posição foi alterada com sucesso");
                break;
            case MAKEIMPORTANT:
                System.out.println("Agora é um utilizador com permissões extra");
                break;
            case DANGER:
                System.out.println("Esteve em contacto com um utilizador que recentemente se declarou como infetado, tenha cuidado e se sentir sintomas ligue para o SNS");
                break;
        }
    }

    public void welcome(String username) {
        System.out.println("Bem-vindo "+username+" !");
    }

    public void numOfPeopleOn(Tuple<Integer,Integer> t,int n) {
        System.out.println("Na posição "+t.toString() + " estão "+n+" utilizadores");
    }

    public void remindEmpty(List<byte[]> data) {
        if (data.isEmpty()) System.out.println("Essa posição está atualmente ocupada mas será notificado quando estiver vazia");
        else {
            Tuple<Integer, Integer> t1 = new Tuple<>(Integer.parseInt(new String(data.get(0))), Integer.parseInt(new String(data.get(1))));
            System.out.println("A posição " + t1.toString() + " está vazia");
        }
    }

    public void mapData(Map<Tuple<Integer,Integer>,Tuple<Integer,Integer>> grelha) {
        //código do mestre Pedro
    }

    public void file(String filename) {
        int data;
        char dataC;
        String color;
        try {
            File f = new File("asciiArt/"+filename);
            FileReader fr=new FileReader(f);   //Creation of File Reader object
            BufferedReader br=new BufferedReader(fr);  //Creation of BufferedReader object
            while ((data=br.read())!=-1) {
                dataC = (char) data;
                switch (dataC) {
                    case '_':
                    case '|':
                        color = Colors.ANSI_RED;
                        break;
                    case '$':
                        color=Colors.ANSI_BLUE;
                        break;
                    default:
                        color=Colors.ANSI_PURPLE;
                        break;
                }
                if ((dataC>64 && dataC<91) || (dataC>96 && dataC<123) || dataC=='ç' || dataC=='ã' || dataC=='á' || dataC=='ú')
                    color=Colors.ANSI_CYAN;
                System.out.print(color + dataC);
            }
            System.out.println(Colors.ANSI_RESET);
            br.close();
        }
        catch (FileNotFoundException e) {
            error("Ficheiro não encontrado");
        }
        catch (IOException e) {
            error("Ocorreu um erro de Input/Output");
        }
    }
}
