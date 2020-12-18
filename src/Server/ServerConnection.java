package Server;

import java.io.*;
import java.net.Socket;
import java.util.Map;

import Model.*;
import Utils.Colors;

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

        try {
            while (socket.isConnected()) {
                String command = in.readUTF();
                String[] splited = command.split(" ");
                try {
                    switch (splited[0]) {
                        case "signup":
                            userID = splited[1];
                            pass = splited[2];
                            if (loggedIn) {
                                out.writeUTF(Colors.ANSI_BLUE + "Já tem sessão iniciada");
                                out.flush();
                                break;
                            }
                            worked = info.addNewUser(userID,pass);
                            if (!worked) out.writeUTF(Colors.ANSI_RED + "Esse nome de utilizador já existe");
                            else  {
                                out.writeUTF(Colors.ANSI_GREEN + "Seja Bem-vindo ao CoronaBYErus, a sua conta foi criada com sucesso use help para saber que comandos pode utilizar");
                                info.addDOAtualToUser(userID,out);
                                loggedIn=true;
                            }
                            out.flush();
                            break;
                        case "login":
                            userID = splited[1];
                            pass = splited[2];
                            if (loggedIn) {
                                out.writeUTF(Colors.ANSI_RED + "Já tem sessão iniciada");
                                out.flush();
                                break;
                            }
                            if (info.isInfetado(userID))
                                out.writeUTF(Colors.ANSI_RED + "Não pode iniciar sessão porque se notificou como infetado, fique em casa e proteja-se");
                            else
                                if (!info.isPassCorreta(userID,pass))
                                    out.writeUTF(Colors.ANSI_RED + "Password incorreta");
                                else {
                                    out.writeUTF(Colors.ANSI_GREEN + "Bem-vindo "+userID +"!");
                                    info.addDOAtualToUser(userID,out);
                                    loggedIn = true;
                                }
                            out.flush();
                            break;
                        case "logout":
                            if (loggedIn) {
                                loggedIn=false;
                                out.writeUTF("A sua sessão foi fechada, não se preocupe as suas notificações irão ser guardadas");
                                info.removeDOAtualDoUser(userID);
                            }
                            else {
                                out.writeUTF(Colors.ANSI_RED + "Não se encontra logado");
                            }
                            out.flush();
                            break;
                        case "changePos":
                            if (!loggedIn) {
                                out.writeUTF(Colors.ANSI_RED + "Não tem sessão iniciada");
                                out.flush();
                                break;
                            }
                            coords = new Tuple<>(Integer.valueOf(splited[1]),Integer.valueOf(splited[2]));
                            if (info.getPosition(userID)!=null && coords.equals(info.getPosition(userID))) {
                                out.writeUTF(Colors.ANSI_RED + "Já se encontra nessa posição");
                            }

                                info.updateCoords(coords, userID);
                                out.writeUTF("A sua posição foi alterada com sucesso");

                            out.flush();
                            break;
                        case "numOfPeopleOn":
                            if (!loggedIn) {
                                out.writeUTF(Colors.ANSI_RED + "Não tem sessão iniciada");
                                out.flush();
                                break;
                            }
                            coords = new Tuple<>(Integer.valueOf(splited[1]),Integer.valueOf(splited[2]));
                            int n = info.getNumOfPeopleOn(coords);
                            out.writeUTF("Na posição " + coords.toString() +" estão " + n + " utilizadores");
                            out.flush();
                            break;
                        case "remindWhenEmpty":
                            Tuple<Integer,Integer> position=new Tuple<>(Integer.valueOf(splited[1]),Integer.valueOf(splited[2]));
                            if (!loggedIn) {
                                out.writeUTF(Colors.ANSI_RED + "Não tem sessão iniciada");
                                out.flush();
                                break;
                            }
                            if (info.getNumOfPeopleOn(position)==0) {
                                out.writeUTF("Esta posição encontra-se vazia");
                            }
                            else {
                                info.addPedido(position,userID);
                                out.writeUTF("Neste momento essa posição encontra-se ocupada, quando estiver vazia irá receber uma notificação");
                            }
                            out.flush();
                            break;
                        case "infected":
                            if (!loggedIn) {
                                out.writeUTF(Colors.ANSI_RED + "Não tem sessão iniciada");
                                out.flush();
                                break;
                            }
                            info.addInfetado(userID);
                            out.writeUTF("Fique em casa e tome cuidado");
                            out.flush();
                            info.removeDOAtualDoUser(userID);
                            loggedIn=false;
                            break;
                        case "quit":
                            out.writeUTF(Colors.ANSI_GREEN + "Obrigado por usar CoronaBYErus");
                            out.flush();
                            info.removeDOAtualDoUser(userID);
                            closeConnection();
                            return;
                        case "help":
                            String extra;
                            if (loggedIn) {
                                 extra = info.isVIP(userID) ?"h1" : "h2";
                            }
                            else extra = "h3";
                            out.writeUTF(extra);
                            out.flush();
                            out.flush();
                            break;
                        case "getMapData":
                            if (!loggedIn) {
                                out.writeUTF(Colors.ANSI_RED + "Não tem sessão iniciada");
                                out.flush();
                                break;
                            }
                            if (info.isVIP(userID)) {

                                for (Map.Entry<Tuple<Integer,Integer>,Tuple<Integer,Integer>> t : info.getMapData().entrySet()) {
                                    out.writeUTF("Posição "+t.getKey().toString()+" teve "+t.getValue().getFirst()+" utilizadores e "+t.getValue().getSecond()+" doentes");
                                }
                            }
                            else out.writeUTF(Colors.ANSI_RED + "Não tem acesso a esta feature");
                            out.flush();
                            break;
                        default:
                            out.writeUTF(Colors.ANSI_RED + "Comando inexistente use " + Colors.ANSI_BLUE +   "help");
                            out.flush();
                            break;
                    }
                }
                catch (IndexOutOfBoundsException e) {
                    out.writeUTF(Colors.ANSI_RED + "Número errado de argumentos");
                    out.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
