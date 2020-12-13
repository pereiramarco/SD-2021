package Server;

import java.io.*;
import java.net.Socket;
import Model.*;

public class ServerConnection implements Runnable {
    private Socket socket;
    private Info info;
    private DataOutputStream out;
    private DataInputStream in;

    public ServerConnection (Socket socket, Info info) throws IOException {
        this.socket = socket;
        this.info = info;
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    public void closeConnection() throws IOException {
        socket.shutdownOutput();
        socket.shutdownInput();
        socket.close();
    }

    @Override
    public void run() {
        boolean worked,loggedIn=false;
        Tuple<Integer,Integer> coords;
        String userID="";
        String pass;
        int opcao=1;

        try {
            while (socket.isConnected()) {
                String command = in.readUTF();
                String[] splited = command.split(" ");
                try {
                    switch (splited[0]) {
                        case "signup":
                            if (loggedIn) {
                                out.writeUTF("ESTÁS LOGADO PALHAÇO");
                                out.flush();
                                break;
                            }
                            userID = splited[1];
                            pass = splited[2];
                            worked = info.addNewUser(userID,pass);
                            if (!worked) out.writeUTF("JA EXISTE");
                            else  {
                                out.writeUTF("OK ADICIONAMOS-TE USA help PRA VER OS COMANDOS");
                                info.addDOAtualToUser(userID,out);
                                loggedIn=true;
                            }
                            out.flush();
                            break;
                        case "login":
                            if (loggedIn) {
                                out.writeUTF("ESTÁS LOGADO PALHAÇO");
                                out.flush();
                                break;
                            }
                            userID = splited[1];
                            pass = splited[2];
                            worked = info.isPassCorreta(userID,pass);
                            if (!worked) out.writeUTF("ERRASTE A PASS");
                            else {
                                out.writeUTF("OK EXISTES USA help PARA VER OS COMANDOS");
                                info.addDOAtualToUser(userID,out);
                                loggedIn = true;
                            }
                            out.flush();
                            break;
                        case "logout":
                            if (loggedIn) {
                                loggedIn=false;
                                out.writeUTF("OK AGORA NÃO TENS ACESSO A QUASE NADA DUMBFUCK VÊ NO help");
                                info.removeDOAtualDoUser(userID);
                            }
                            else {
                                out.writeUTF("NÃO ESTAS LOGADO");
                            }
                            out.flush();
                            break;
                        case "changePos":
                            if (!loggedIn) {
                                out.writeUTF("NÃO TE ARMES");
                                out.flush();
                                break;
                            }
                            coords = new Tuple<>(Integer.valueOf(splited[1]),Integer.valueOf(splited[2]));
                            info.updateCoords(coords,userID);
                            out.writeUTF("POSIÇÃO MUDADA");
                            out.flush();
                            break;
                        case "numOfPeopleOn":
                            if (!loggedIn) {
                                out.writeUTF("NÃO TE ARMES");
                                out.flush();
                                break;
                            }
                            coords = new Tuple<>(Integer.valueOf(splited[1]),Integer.valueOf(splited[2]));
                            int n = info.getNumOfPeopleOn(coords);
                            out.writeUTF("ESTÃO LÁ " + n + " PESSOAS BURRO");
                            out.flush();
                            break;
                        case "remindWhenEmpty":
                            if (!loggedIn) {
                                out.writeUTF("NÃO TE ARMES");
                                out.flush();
                                break;
                            }
                            break;
                        case "infected":
                            if (!loggedIn) {
                                out.writeUTF("NÃO TE ARMES");
                                out.flush();
                                break;
                            }
                            info.addInfetado(userID);
                            out.writeUTF("BONITO SERVIÇO PANELEIRO");
                            out.flush();
                            info.removeDOAtualDoUser(userID);
                            closeConnection();
                            return;
                        case "quit":
                            loggedIn=false;
                            out.writeUTF("quit");
                            out.flush();
                            info.removeDOAtualDoUser(userID);
                            closeConnection();
                            return;
                        case "help":
                            if (loggedIn) {
                                out.writeUTF("-----------CoronaBYErus-----------\nchangePos x y -> Mudar Localização\nnumOfPeopleOn x y -> Número de pessoas numa posição\nremindWhenEmpty x y -> Marcar lembrete de local quando vazio\ninfected -> Comunicar que está infetado\nhelp -> Ajuda\nlogout -> Terminar sessão\nquit -> Sair");
                            }
                            else out.writeUTF("-----------CoronaBYErus-----------\nlogin username password -> Iniciar sessão\nsignup username password -> Criar conta\nhelp -> Ajuda");
                            out.flush();
                            break;
                        default:
                            out.writeUTF("DEVES COMER MERDA AS COLHERES USA help PANELEIRO");
                            out.flush();
                            break;
                    }
                }
                catch (IndexOutOfBoundsException e) {
                    System.out.println("Número errado de argumentos");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
