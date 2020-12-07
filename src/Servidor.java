import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Tuplo para Coordenadas
class Tuplo<T,U> {
    T first;
    U second;

    Tuplo(T f, U s) {
        first=f;
        second=s;
    }

    T getFirst() {
        return first;
    }

    U getSecond() {
        return second;
    }
}

// Informação que o Servidor tem de reter
class Info {
    private Map<Tuplo<Integer,Integer>,HashSet<String>> mapa; // a chave será a localização e o value será uma lista dos ids dos utilizadores que estão nessa mesma localização
    private Map<String,List<String>> cruzados; //a chave será um id do utilizador e a lista será todos os utilizadores com quem se cruzou
    private Map<String,Boolean> infetados; //a chave será o id de um utilizador e o value será um boolean que será true caso esteja infetado e false caso contrário

    public void updateCoords(Tuplo<Integer,Integer> posicaoAtual,Tuplo<Integer,Integer> posicaoAntiga,String id) {
        mapa.get(posicaoAntiga).remove(id);
        mapa.get(posicaoAtual).add(id);
    }

    public void addUser(Tuplo<Integer,Integer> pos,String user) {
        mapa.get(pos).add(user);
        cruzados.put(user,new ArrayList<>());
        infetados.put(user,false);
    }

    public void addInfetado(String user) {
        infetados.put(user,true);
    }
}

public class Servidor {

}
