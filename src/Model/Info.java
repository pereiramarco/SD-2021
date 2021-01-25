package Model;

import Server.NotifyInfectedContact;
import Server.TaggedConnection;
import Utils.Tuple;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Informação que o Servidor tem de reter
public class Info {
    private Tuple<Integer,Integer> mapDimensions;
    private Map<Tuple<Integer,Integer>, InfoPosicao> mapa; //a chave será a localização e o value será a Classe Posição
    private Map<String,User> users; //todos os users no sistema
    private Set<String> vipUsers; // utilizadores que são vip
    private ReentrantReadWriteLock l;
    private ReentrantReadWriteLock.ReadLock rl;
    private ReentrantReadWriteLock.WriteLock wl;

    public Info() {
        mapDimensions = new Tuple<>(12,12);
        mapa = new HashMap<>();
        users = new HashMap<>();
        l = new ReentrantReadWriteLock();
        rl = l.readLock();
        wl = l.writeLock();
        vipUsers=new HashSet<>();
        for(int i = 0; i < mapDimensions.getFirst(); i++)
            for(int j = 0 ; j < mapDimensions.getSecond(); j++)
                mapa.put(new Tuple<>(i,j),new InfoPosicao());
    }

    public Tuple<Integer, Integer> getMapDimensions() {
        return mapDimensions;
    }

    public void updateCoords(Tuple<Integer,Integer> pos,String id) {
        try {
            wl.lock();
            User user = users.get(id);
            Tuple<Integer,Integer> poss=user.getPosicao();
            mapa.get(pos).addUser(id);
            if (poss!=null && mapa.containsKey(poss)) { // se tiver posição anterior remove-o dessa posição
                mapa.get(poss).removeUser(id);
            }
            user.setPosicao(pos); // atualiza posição do user
            for (String userID : mapa.get(pos).getUsersPosAtual()) { // para todos os useres na posição atual do user adiciona-os aos encontros do user que se moveu agora para lá e adiciona o user aos encontros dos que estavam nessa localização
                if (!userID.equals(id)) {
                    user.addEncontro(userID);
                    users.get(userID).addEncontro(user.getUsername());
                }
            }
        } finally {
            wl.unlock();
        }
    }

    public boolean addNewUser(String username, String password) {
        try {
            wl.lock();
            if (users.containsKey(username)) { // se já existir o user retorna falso
                return false;
            } else {
                users.put(username, new User(username, password)); //adiciona o novo user
            }
            return true;
        }
        finally {
            wl.unlock();
        }
    }

    public void addDOAtualToUser(String user, TaggedConnection taggedConnection) {
        try {
            wl.lock();
            users.get(user).setTCatual(taggedConnection); //modifica a taggedConnection do user visto que ele pode dar login noutra sessão de terminal
            signalPedidosTodos();
        }
        finally {
            wl.unlock();
        }
    }

    public void removeDOAtualDoUser(String user) {
        try {
            wl.lock();
            users.get(user).setTCatual(null); // remove taggedConnection do user caso ele faça logout ou feche a sessão na sua conta de alguma forma
        }
        finally {
            wl.unlock();
        }
    }

    public void addInfetado(String user) throws IOException {
        try {
            wl.lock();
            User u = users.get(user);
            u.setInfetado();
            mapa.get(u.getPosicao()).removeUser(u.username); // remove o infetado da posição pois assume-se que ele volta para casa
            u.setPosicao(null);
            for (String us : u.getEncontros()) { // notifica cada um dos users com quem esteve em contacto de que estiveram emc ontacto com um infetado
                TaggedConnection tCC = users.get(us).getTCatual();
                if (tCC!=null) {
                    Thread th = new Thread(new NotifyInfectedContact(tCC));
                    th.start();
                }
                else {
                    users.get(us).setDeveSerNotificado(true);
                }
            }
        } finally {
            wl.unlock();
        }
    }

    public boolean isPassCorreta(String userID,String pass) {
        try {
            wl.lock();
            return users.get(userID).passwordCorreta(pass);
        }
        finally {
            wl.unlock();
        }
    }

    public int getNumOfPeopleOn(Tuple<Integer, Integer> pos) {
        try {
            rl.lock();
            return mapa.get(pos).nmrPeopleOn();
        }
        finally {
            rl.unlock();
        }
    }

    public boolean isInfetado(String userID) {
        try {
            rl.lock();
            return users.containsKey(userID) && users.get(userID).isInfetado();
        }
        finally {
            rl.unlock();
        }
    }

    public boolean isVIP(String userID) {
        try {
            rl.lock();
            return vipUsers.contains(userID);
        }
        finally {
            rl.unlock();
        }
    }

    public void addVIP(String userID) {
        try {
            wl.lock();
            vipUsers.add(userID);
        }
        finally {
            wl.unlock();
        }
    }

    public Map<Tuple<Integer,Integer>,Tuple<Integer,Integer>> getMapData() {
        Map<Tuple<Integer,Integer>,Tuple<Integer,Integer>> res = new HashMap<>();
        try {
            rl.lock();
            for (Map.Entry<Tuple<Integer,Integer>, InfoPosicao> t : mapa.entrySet()) { // em todas as posições verifica se para cada user no Set dessa posição está infetado ou não
                int u=0,d=0;
                for (String s : t.getValue().getHistorico()) {
                    if (isInfetado(s))
                        d++;
                    else u++;
                }
                if(u > 0 || d > 0)
                    res.put(t.getKey(),new Tuple<>(u,d));
            }

            return res;
        }
        finally {
            rl.unlock();
        }
    }

    public Tuple<Integer, Integer> getPosition(String userID) {
        try {
            rl.lock();
            return users.get(userID).getPosicao();
        }
        finally {
            rl.unlock();
        }
    }

    public InfoPosicao getPosicao(Tuple<Integer,Integer> coord) {
        try {
            rl.lock();
            return mapa.get(coord);
        } finally {
            rl.unlock();
        }
    }


    public void signalPedidos(Tuple<Integer,Integer> pos) {
        try {
            rl.lock();
            mapa.get(pos).signallAll();
        } finally {
            rl.unlock();
        }
    }


    private void signalPedidosTodos() {
        try {
            rl.lock();
            mapa.values().forEach(InfoPosicao::signallAll);
        } finally {
            rl.unlock();
        }
    }

    public boolean userExiste(String username) {
        try {
            rl.lock();
            return users.containsKey(username);
        }
        finally {
            rl.unlock();
        }
    }

    public User getUser(String userID) {
        try {
            rl.lock();
            return users.get(userID);
        }
        finally {
            rl.unlock();
        }
    }

    public boolean userDeveSerNotificado(String username) {
        try {
            rl.lock();
            return getUser(username).isDeveSerNotificado();
        }
        finally {
            rl.unlock();
        }
    }

    public void setUserDeveSerNotificado(String username,boolean b) {
        try {
            wl.lock();
            getUser(username).setDeveSerNotificado(b);
        }
        finally {
            wl.unlock();
        }
    }
}