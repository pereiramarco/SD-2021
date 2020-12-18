package Model;

import Server.Notifier;
import Utils.Colors;

import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Informação que o Servidor tem de reter
public class Info {
    private Map<Tuple<Integer,Integer>, Set<String>> mapa; //a chave será a localização e o value será uma lista dos ids dos utilizadores que estão nessa mesma localização
    private Map<String,User> users; //todos os users no sistema
    private Map<Tuple<Integer,Integer>,Set<String>> pedidos;
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
            if (poss!=null && mapa.containsKey(poss)) {
                mapa.get(poss).remove(id);
                if (mapa.get(poss).isEmpty() && pedidos.containsKey(poss)) {
                    Notifier n = new Notifier(users,pedidos.get(poss), Colors.ANSI_BLUE + "A posição " + poss.toString() + " está livre!!  " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
                    Thread th = new Thread(n);
                    th.start();
                    th.join(); //só necessário se em baixo for
                    pedidos.remove(poss); //devemos apagar a lista de pedidos após uma iteração ou o user deve receber para smepre estas notificações
                }
            }
            if (mapa.containsKey(pos)) {
                s = mapa.get(pos);
            } else {
                s = new HashSet<>();
            }
            s.add(id);
            mapa.put(pos, s);
            user.setPosicao(pos);
            for (String userID : mapa.get(pos)) {
                if (!userID.equals(id)) {
                    user.addEncontro(userID);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            wl.unlock();
        }
    }

    public boolean addNewUser(String user,String pass) {
        try {
            wl.lock();
            if (users.containsKey(user)) {
                return false;
            } else {
                users.put(user, new User(user,pass,false));
            }
            return true;
        }
        finally {
            wl.unlock();
        }
    }

    public void addDOAtualToUser(String user,DataOutputStream dataOutput) {
        try {
            wl.lock();
            users.get(user).setdOatual(dataOutput);
        }
        finally {
            wl.unlock();
        }
    }

    public void removeDOAtualDoUser(String user) {
        try {
            wl.lock();
            users.get(user).setdOatual(null);
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
            mapa.get(u.getPosicao()).remove(u.username);
            u.setPosicao(null);
            Thread th = new Thread(new Notifier(users,u.getEncontros(),Colors.ANSI_BLUE + "Esteve em contacto com um utilizador que se declarou infetado, tome cuidado se tiver sintomas ligue para os serviços de saúde nacional " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))));
            th.start();
        } finally {
            wl.unlock();
        }
    }

    public void addPedido(Tuple<Integer,Integer> coords,String userID) {
        Set<String> t;
        try {
            wl.lock();
            if (pedidos.containsKey(coords)) {
                t = pedidos.get(coords);
            }
            else {
                t = new HashSet<>();
                pedidos.put(coords,t);
            }
            t.add(userID);
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
            for (Map.Entry<Tuple<Integer,Integer>,Set<String>> t : posicoes.entrySet()) {
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
}