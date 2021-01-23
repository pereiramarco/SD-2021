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
import Utils.Frame;
import Utils.Tag;

public class ServerConnection implements Runnable {
    private final TaggedConnection tC;
    private final Info info;
    private String username; // username fo utilizador a utilizar a conexão de momento
    private boolean loggedIn; // se o cliente que está a usar a conexão já se autenticou
    private boolean online;

    public ServerConnection (TaggedConnection tCG, Info info) {
        this.tC = tCG;
        this.info = info;
        this.username=null;
        this.loggedIn=false;
        this.online = true;
    }

    @Override
    public void run() {
        try {
            while (this.online) {
                Frame command = this.tC.receive();
                List<byte[]> data = new ArrayList<>(); //data a enviar no frame de resposta
                try {
                    switch (command.tag) {
                        case SIGNUP -> signup(command.data);
                        case LOGIN -> login(command.data);
                        case LOGOUT -> logout();
                        case CHANGEPOS -> changePos(command.data);
                        case NUMOFPEOPLEON -> numPeopleOn(command.data);
                        case REMINDWHENEMPTY -> remindWhenEmpty(command.data);
                        case INFECTED -> infected();
                        case QUIT -> quit();
                        case HELP -> help();
                        case GETMAPDATA -> getMapData(username);
                        case MAKEIMPORTANT -> makeImportant();
                    }
                }
                catch (IndexOutOfBoundsException e) {
                    data.add("Número errado de argumentos".getBytes());
                    this.tC.send(new Frame(Tag.ERROR,data));
                } catch (LoggedInException | UtilizadorExistenteException | PassIncorretaException | JaNessaPosicaoException e) {
                    data.add(e.getMessage().getBytes());
                    this.tC.send(new Frame(Tag.ERROR,data));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void signup(List<byte[]> commandData) throws UtilizadorExistenteException, IOException, LoggedInException {
        if (this.loggedIn) {
            throw new LoggedInException("Já tem sessão iniciada");
        }

        String username = new String(commandData.get(0));
        String password = new String(commandData.get(1));

        boolean worked = this.info.addNewUser(username, password);
        if (!worked) throw new UtilizadorExistenteException("Utilizador já existente");
        else  {
            this.username=username;
            this.loggedIn=true;
            this.tC.send(new Frame(Tag.SIGNUP,null));
            this.info.addDOAtualToUser(username,this.tC); // se o login for sucessful queremos mudar a TaggedConnection
        }
    }

    private void login(List<byte[]> commandData) throws LoggedInException, UtilizadorExistenteException, PassIncorretaException, IOException {
        if (this.loggedIn) {
            throw new LoggedInException("Já tem sessão iniciada");
        }

        String username = new String(commandData.get(0));
        String password = new String(commandData.get(1));

        if (this.info.isInfetado(username))
            throw new LoggedInException("Está infetado e por isso não pode iniciar sessão");
        else {
            if (!this.info.userExiste(username))
                throw new UtilizadorExistenteException("Esse Utilizador não existe");
            else if (!this.info.isPassCorreta(username, password))
                throw new PassIncorretaException("Password incorreta");
            else {
                List<byte[]> data = new ArrayList<>(); //data a enviar no frame de resposta
                this.username=username;
                this.loggedIn=true;
                data.add(username.getBytes()); //envia o userID que deu login para permitir apresentar no ecrã de forma bonita
                this.tC.send(Tag.LOGIN, data);
                this.info.addDOAtualToUser(username, this.tC); // se o login for sucessful queremos mudar a TaggedConnection
            }
        }
    }

    private void logout() throws LoggedInException, IOException {
        if (this.loggedIn) {
            this.loggedIn=false;
            this.info.removeDOAtualDoUser(this.username); // se o logout for sucessful queremos mudar a TaggedConnection associada ao user
            this.username=null;
                this.tC.send(new Frame(Tag.LOGOUT,null));
        }
        else {
            throw new LoggedInException("Não tem sessão iniciada");
        }
    }

    private void changePos(List<byte[]> commandData) throws LoggedInException, JaNessaPosicaoException, IOException {
        if (!this.loggedIn) {
            throw new LoggedInException("Não tem sessão iniciada");
        }

        Tuple<Integer,Integer> coords = new Tuple<>(Integer.valueOf(new String(commandData.get(0))),
                Integer.valueOf(new String(commandData.get(1))));
        if (this.info.getPosition(username)!=null && coords.equals(this.info.getPosition(username))) {
            throw new JaNessaPosicaoException("Já se encontra nesta posicao");
        }

        Tuple<Integer,Integer> posicaoAnterior=this.info.getPosition(username);
        this.info.updateCoords(coords, username);
        if (posicaoAnterior!=null && this.info.getNumOfPeopleOn(posicaoAnterior)==0) { //  se a posição de onde saiu for nula avisa os pedidos de remindWhenEmpty
            this.info.signalPedidos(posicaoAnterior);
        }

        this.tC.send(new Frame(Tag.CHANGEPOS,null));
    }

    private void numPeopleOn(List<byte[]> commandData) throws LoggedInException, IOException {
        if (!this.loggedIn) {
            throw new LoggedInException("Não tem sessão iniciada");
        }

        Tuple<Integer,Integer> coords = new Tuple<>(Integer.valueOf(new String(commandData.get(0))),
                Integer.valueOf(new String(commandData.get(1))));
        int numberPeopleOnCoords = this.info.getNumOfPeopleOn(coords);

        List<byte[]> data = new ArrayList<>(); //data a enviar no frame de resposta
        data.add(String.valueOf(coords.getFirst()).getBytes()); //adiciona X
        data.add(String.valueOf(coords.getSecond()).getBytes()); //adiciona Y
        data.add(String.valueOf(numberPeopleOnCoords).getBytes()); // adiciona o numero de pessoas nessa posição
        this.tC.send(Tag.NUMOFPEOPLEON,data);
    }

    private void remindWhenEmpty(List<byte[]> commandData) throws LoggedInException {
        if (!this.loggedIn) {
            throw new LoggedInException("Não tem sessão iniciada");
        }

        Tuple<Integer,Integer> position=new Tuple<>(Integer.valueOf(new String(commandData.get(0))),
                Integer.valueOf(new String(commandData.get(1))));
        // lançar uma classe que ficará à espera que a posição fique vazia
        NotifierEmptyPosition net = new NotifierEmptyPosition(this.info,username,position);
        Thread th  = new Thread(net);

        th.start();
    }

    private void infected() throws LoggedInException, IOException {
        if (!this.loggedIn) {
            throw new LoggedInException("Não tem sessão iniciada");
        }
        Tuple<Integer,Integer> pos = this.info.getPosition(username);

        this.info.addInfetado(username);
        if (this.info.getNumOfPeopleOn(pos)==0) // sinaliza os pedidos se a posição onde o user infetado estava tiver vazia agora
            this.info.signalPedidos(pos);

        this.loggedIn=false;
        this.info.removeDOAtualDoUser(username);

        this.tC.send(new Frame(Tag.INFECTED,null));
    }

    private void quit() throws IOException {
        if(loggedIn) {
            this.info.removeDOAtualDoUser(username);
        }

        this.tC.send(new Frame(Tag.QUIT,null));
        this.tC.close();
        this.online = false;
    }

    private void help() throws IOException {
        String filename; // ficheiro onde está o menu de help
        if (this.loggedIn) {
            filename = this.info.isVIP(username) ? "h1" : "h2"; // se estiver logado e for VIP irá apresentar um menu de ajudas diferente
        }
        else filename = "h3";

        List<byte[]> data = new ArrayList<>(); //data a enviar no frame de resposta
        data.add(filename.getBytes());
        this.tC.send(new Frame(Tag.HELP,data));
    }

    private void getMapData(String username) throws LoggedInException, IOException {
        if (!this.loggedIn) {
            throw new LoggedInException("Não tem sessão iniciada");
        }

        if (this.info.isVIP(username)) {
            for (Map.Entry<Tuple<Integer,Integer>,Tuple<Integer,Integer>> t : this.info.getMapData().entrySet()) {
                List<byte[]> data = new ArrayList<>(); //data a enviar no frame de resposta
                Tuple<Integer,Integer> posicao = t.getKey();
                int numeroUsers = t.getValue().getFirst();
                int numeroInfetados = t.getValue().getSecond();
                data.add(String.valueOf(posicao.getFirst()).getBytes()); // adiciona X
                data.add(String.valueOf(posicao.getSecond()).getBytes()); // adiciona Y
                data.add(String.valueOf(numeroUsers).getBytes()); //adiciona utilizadores que estiveram em  X Y
                data.add(String.valueOf(numeroInfetados).getBytes()); //adiciona doentes que estiveram em X Y
                this.tC.send(new Frame(Tag.GETMAPDATA,data));
            }
        } else {
            throw new LoggedInException("Não tem acesso a esta feature");
        }
    }

    private void makeImportant() throws IOException {
        this.info.addVIP(username);
        this.tC.send(new Frame(Tag.MAKEIMPORTANT,null));
    }
}
