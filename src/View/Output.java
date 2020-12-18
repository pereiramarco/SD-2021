package View;

import Utils.Colors;

import java.io.*;
import java.util.Scanner;

public class Output {
    BufferedReader userIn;

    public Output() {
        userIn=new BufferedReader(new InputStreamReader(System.in));
    }

    public String read() throws IOException {
        return userIn.readLine();
    }

    public void error(String s) {
        System.out.println(Colors.ANSI_RED + s);
    }

    public void info(String reply) {
        System.out.println(reply+ Colors.ANSI_RESET);
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
