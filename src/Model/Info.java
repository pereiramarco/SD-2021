package Model;

import Server.NotifierEmptyPosition;
import Server.NotifyInfectedContact;
import Server.TaggedConnection;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Informação que o Servidor tem de reter
public class Info {
    private Map<Tuple<Integer,Integer>, Set<String>> mapa; //a chave será a localização e o value será uma lista dos ids dos utilizadores que estão nessa mesma localização
    private Map<String,User> users; //todos os users no sistema
    private Map<Tuple<Integer,Integer>,Set<NotifierEmptyPosition>> pedidos; // Set de pedidos de remindWhenEmpty por cada posição no Map
    private Set<String> vipUsers; // utilizadores que são vip
    private Map<Tuple<Integer,Integer>,Set<String>> posicoes; // mapa de posições para lista de utilizadores que já lá estiveram
    private ReentrantReadWriteLock l;
    private ReentrantReadWriteLock.ReadLock rl;
    private ReentrantReadWriteLock.WriteLock wl;

    public Info() {
        mapa = new HashMap<>();
        users = new HashMap<>();
        l = new ReentrantReadWriteLock();
        rl = l.readLock();
        wl = l.writeLock();
        pedidos=new HashMap<>();
        vipUsers=new HashSet<>();
        posicoes=new HashMap<>();
    }

    public void updateCoords(Tuple<Integer,Integer> pos,String id) {
        try {
            wl.lock();
            Set<String> s;
            User user = users.get(id);
            Tuple<Integer,Integer> poss=user.getPosicao();
            Set<String> us = posicoes.computeIfAbsent(pos, k -> new HashSet<>());
            us.add(id);
            if (poss!=null && mapa.containsKey(poss)) { // se tiver posição anterior remove-o dessa posição
                mapa.get(poss).remove(id);
            }
            if (mapa.containsKey(pos)) { // checka se o mapa já tem info sobre essa posição
                s = mapa.get(pos);
            } else {
                s = new HashSet<>();
            }
            s.add(id);
            mapa.put(pos, s);
            user.setPosicao(pos); // atualiza posição do user
            for (String userID : mapa.get(pos)) { // para todos os useres na posição atual do user adiciona-os aos encontros do user que se moveu agora para lá e adiciona o user aos encontros dos que estavam nessa localização
                if (!userID.equals(id)) {
                    user.addEncontro(userID);
                    users.get(userID).addEncontro(user.getUsername());
                }
            }
        } finally {
            wl.unlock();
        }
    }

    public boolean addNewUser(Tuple<String,String> userInfo) {
        try {
            wl.lock();
            if (users.containsKey(userInfo.first)) { // se já existir o user retorna falso
                return false;
            } else {
                users.put(userInfo.first, new User(userInfo.first,userInfo.second,false)); //adiciona o novo user
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
            mapa.get(u.getPosicao()).remove(u.username); // remove o infetado da posição pois assume-se que ele volta para casa
            u.setPosicao(null);
            for (String us : u.getEncontros()) { // notifica cada um dos users com quem esteve em contacto de que estiveram emc ontacto com um infetado
                Thread th = new Thread(new NotifyInfectedContact(users.get(us).getTCatual()));
                th.start();
            }
        } finally {
            wl.unlock();
        }
    }

    public void addPedido(Tuple<Integer,Integer> coords,NotifierEmptyPosition c) {
        Set<NotifierEmptyPosition> t;
        try {
            wl.lock();
            if (pedidos.containsKey(coords)) {
                t = pedidos.get(coords);
            }
            else {
                t = new HashSet<>();
                pedidos.put(coords,t); // adiciona os pedidos de remindWhenEmpty à posiçºao onde pertencem
            }
            t.add(c);
        }
        finally {
            wl.unlock();
        }
    }

    public boolean isPassCorreta(String userID,String pass) {
        try {
            wl.lock();
            return users.containsKey(userID) && users.get(userID).passwordCorreta(pass);
        }
        finally {
            wl.unlock();
        }
    }

    public int getNumOfPeopleOn(Tuple<Integer, Integer> pos) {
        int r=0;
        try {
            rl.lock();
            if (mapa.containsKey(pos)) r=mapa.get(pos).size();
            return r;
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
        Map<Tuple<Integer,Integer>,Tuple<Integer,Integer>> mapa=new HashMap<>();
        try {
            rl.lock();
            for (Map.Entry<Tuple<Integer,Integer>,Set<String>> t : posicoes.entrySet()) { // em todas as posições verifica se para cada user no Set dessa posição está infetado ou não
                int u=0,d=0;
                for (String s : t.getValue()) {
                    if (isInfetado(s))
                        d++;
                    else u++;
                }
                mapa.put(t.getKey(),new Tuple<>(u,d));
            }

            return mapa;
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

    public void signalPedidos(Tuple<Integer,Integer> pos) {
        if (pedidos.containsKey(pos)) {
            Set<NotifierEmptyPosition> c = pedidos.get(pos);
            for (NotifierEmptyPosition cc : c) { // dá signal a cada um dos pedidos de remindWhenEmpty quando a posição fica finalmente vazia
                cc.signal();
            }
        }
    }

    public User getUser(String userID) {
        return users.get(userID);
    }
}