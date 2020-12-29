package Server;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Exceptions.JaNessaPosicaoException;
import Exceptions.LoggedInException;
import Exceptions.PassIncorretaException;
import Exceptions.UtilizadorExistenteException;
import Model.*;
import Utils.Colors;
import Utils.Frame;
import Utils.Tag;

public class ServerConnection implements Runnable {
    private TaggedConnection tC;
    private Info info;

    public ServerConnection (TaggedConnection tCG, Info info) {
        this.tC = tCG;
        this.info = info;
    }

    public void closeConnection() throws IOException {
        tC.close();
    }

    @Override
    public void run() {
        boolean worked,loggedIn=false;
        Tuple<Integer,Integer> coords;
        Tuple<String,String> userInfo=null;

        try {
            while (tC.isConnected()) {
                Frame command = tC.receive();
                List<byte[]> data = new ArrayList<>(); //data a enviar no frame de resposta
                try {
                    switch (command.tag) {
                        case SIGNUP:
                            userInfo = new Tuple<>(new String(command.data.get(0)),new String(command.data.get(1)));
                            if (loggedIn) {
                                throw new LoggedInException("Já tem sessão iniciada");
                            }
                            worked = info.addNewUser(userInfo);
                            if (!worked) throw new UtilizadorExistenteException("Utilizador já existente");
                            else  {
                                tC.send(new Frame(Tag.SIGNUP,data));
                                info.addDOAtualToUser(userInfo.getFirst(),tC); // se o login for sucessful queremos mudar a TaggedConnection
                                loggedIn=true;
                            }
                            break;
                        case LOGIN:
                            userInfo = new Tuple<>(new String(command.data.get(0)),new String(command.data.get(1)));
                            if (loggedIn) {
                                throw new LoggedInException("Já tem sessão iniciada");
                            }
                            if (info.isInfetado(userInfo.getFirst()))
                                throw new LoggedInException("Está infetado e por isso não pode iniciar sessão");
                            else
                                if (!info.isPassCorreta(userInfo.getFirst(),userInfo.getSecond()))
                                    throw new PassIncorretaException("Password incorreta");
                                else if (info.getUser(userInfo.getFirst())==null) throw new UtilizadorExistenteException("Esse Utilizador não existe");
                                else {
                                    data.add(userInfo.getFirst().getBytes()); //envia o userID que deu login para permitir apresentar no ecrã de forma bonita
                                    tC.send(Tag.LOGIN,data);
                                    info.addDOAtualToUser(userInfo.getFirst(), tC); // se o login for sucessful queremos mudar a TaggedConnection
                                    loggedIn = true;
                                }
                            break;
                        case LOGOUT:
                            if (loggedIn) {
                                loggedIn=false;
                                tC.send(new Frame(Tag.LOGOUT,new ArrayList<>()));
                                info.removeDOAtualDoUser(userInfo.getFirst()); // se o logout for sucessful queremos mudar a TaggedConnection
                            }
                            else {
                                throw new LoggedInException("Não tem sessão iniciada");
                            }
                            break;
                        case CHANGEPOS:
                            if (!loggedIn) {
                                throw new LoggedInException("Não tem sessão iniciada");
                            }
                            coords = new Tuple<>(Integer.valueOf(new String(command.data.get(0))),Integer.valueOf(new String(command.data.get(1))));
                            if (info.getPosition(userInfo.getFirst())!=null && coords.equals(info.getPosition(userInfo.getFirst()))) {
                                throw new JaNessaPosicaoException("Já se encontra nesta posicao");
                            }
                            Tuple<Integer,Integer> r=info.getPosition(userInfo.getFirst());
                            info.updateCoords(coords, userInfo.getFirst());
                            if (r!=null && info.getNumOfPeopleOn(r)==0) { //  se a posição de onde saiu for nula avisa os pedidos de remindWhenEmpty
                                info.signalPedidos(r);
                            }
                            tC.send(new Frame(Tag.CHANGEPOS,data));
                            break;
                        case NUMOFPEOPLEON:
                            if (!loggedIn) {
                                throw new LoggedInException("Não tem sessão iniciada");
                            }
                            coords = new Tuple<>(Integer.valueOf(new String(command.data.get(0))),Integer.valueOf(new String(command.data.get(1))));
                            int n = info.getNumOfPeopleOn(coords);
                            data.add(String.valueOf(coords.getFirst()).getBytes()); //adiciona X
                            data.add(String.valueOf(coords.getSecond()).getBytes()); //adiciona Y
                            data.add(String.valueOf(n).getBytes()); // adiciona o numero de pessoas nessa posição
                            tC.send(Tag.NUMOFPEOPLEON,data);
                            break;
                        case REMINDWHENEMPTY:
                            Tuple<Integer,Integer> position=new Tuple<>(Integer.valueOf(new String(command.data.get(0))),Integer.valueOf(new String(command.data.get(1))));
                            if (!loggedIn) {
                                throw new LoggedInException("Não tem sessão iniciada");
                            }
                            NotifierEmptyPosition not = new NotifierEmptyPosition(info,info.getUser(userInfo.getFirst()),position);
                            Thread th  = new Thread(not);
                            th.start();
                            info.addPedido(position,not); //adiciona pedido de RemindWhenEmpty
                            break;
                        case INFECTED:
                            if (!loggedIn) {
                                throw new LoggedInException("Não tem sessão iniciada");
                            }
                            Tuple<Integer,Integer> pos = info.getPosition(userInfo.getFirst());
                            info.addInfetado(userInfo.getFirst());
                            if (info.getNumOfPeopleOn(pos)==0) // sinaliza os pedidos se a posição onde o user infetado estava tiver vazia agora
                                info.signalPedidos(pos);
                            tC.send(new Frame(Tag.INFECTED,new ArrayList<>()));
                            info.removeDOAtualDoUser(userInfo.getFirst());
                            loggedIn=false;
                            break;
                        case QUIT:
                            tC.send(new Frame(Tag.QUIT,new ArrayList<>()));
                            info.removeDOAtualDoUser(userInfo.getFirst());
                            closeConnection();
                            return;
                        case HELP:
                            String filename;
                            if (loggedIn) {
                                 filename = info.isVIP(userInfo.getFirst()) ?"h1" : "h2"; // se estiver logado e for VIP irá apresentar um menu de ajudas diferente
                            }
                            else filename = "h3";
                            data.add(filename.getBytes());
                            tC.send(new Frame(Tag.HELP,data));
                            break;
                        case GETMAPDATA:
                            if (!loggedIn) {
                                throw new LoggedInException("Não tem sessão iniciada");
                            }
                            if (info.isVIP(userInfo.getFirst())) {

                                for (Map.Entry<Tuple<Integer,Integer>,Tuple<Integer,Integer>> t : info.getMapData().entrySet()) {
                                    data.add(String.valueOf(t.getKey().getFirst()).getBytes()); // adiciona X
                                    data.add(String.valueOf(t.getKey().getSecond()).getBytes()); // adiciona Y
                                    data.add(String.valueOf(t.getValue().getFirst()).getBytes()); //adiciona utilziadores que estiveram em  X Y
                                    data.add(String.valueOf(t.getValue().getSecond()).getBytes()); //adiciona doentes que estiveram em X Y
                                    tC.send(new Frame(Tag.GETMAPDATA,data));
                                }
                            }
                            else throw new LoggedInException("Não tem acesso a esta feature");
                            break;
                        case MAKEIMPORTANT:
                            info.addVIP(userInfo.getFirst());
                            tC.send(new Frame(Tag.MAKEIMPORTANT,data));
                            break;
                    }
                }
                catch (IndexOutOfBoundsException e) {
                    data.add("Número errado de argumentos".getBytes());
                    tC.send(new Frame(Tag.ERROR,data));
                } catch (LoggedInException | UtilizadorExistenteException | PassIncorretaException | JaNessaPosicaoException e) {
                    data.add(e.getMessage().getBytes());
                    tC.send(new Frame(Tag.ERROR,data));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
