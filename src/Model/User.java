package Model;

import Server.TaggedConnection;
import Utils.Tuple;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class User {
    String username;
    String password;
    Tuple<Integer,Integer> posicao;
    boolean infetado;
    boolean deveSerNotificado; // boolean que indica se user deve ser notifcado de ter estado em contacto, será usado quando o user não estava logado e a notificação aconteceu
    Set<String> encontros; //users com quem se encontrou
    TaggedConnection dOatual; //TaggedConnection do user caso esteja logado senão será null
    ReentrantLock l = new ReentrantLock();

    public User(String username,String password) {
        this.username=username;
        this.password=password;
        this.posicao= null;
        this.infetado=false;
        this.encontros = new HashSet<>();
        this.deveSerNotificado=false;
    }

    public boolean passwordCorreta(String pass) {
        try {
            l.lock();
            return pass.equals(password);
        }
        finally {
            l.unlock();
        }
    }

    public boolean isInfetado() {
        try {
            l.lock();
            return infetado;
        }
        finally {
            l.unlock();
        }
    }

    public void setInfetado() {
        try {
            l.lock();
            this.infetado = true;
        }
        finally {
            l.unlock();
        }
    }

    public Tuple<Integer, Integer> getPosicao() {
        try {
            l.lock();
            return posicao;
        }
        finally {
            l.unlock();
        }
    }

    public void setPosicao(Tuple<Integer, Integer> posicao) {
        try {
            l.lock();
            this.posicao = posicao;
        }
        finally {
            l.unlock();
        }
    }

    public Set<String> getEncontros() {
        try {
            l.lock();
            return encontros;
        }
        finally {
            l.unlock();
        }
    }

    public void addEncontro(String userID) {
        try {
            l.lock();
            this.encontros.add(userID);
        }
        finally {
            l.unlock();
        }
    }

    public void setTCatual(TaggedConnection dO) {
        try {
            l.lock();
            dOatual=dO;
        }
        finally {
            l.unlock();
        }
    }

    public TaggedConnection getTCatual() {
        try {
            l.lock();
            return dOatual;
        }
        finally {
            l.unlock();
        }
    }

    public String getUsername() {
        try {
            l.lock();
            return username;
        }
        finally {
            l.unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        try {
            l.lock();
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return username.equals(user.getUsername()) && password.equals(user.getPassword());
        }
        finally {
            l.unlock();
        }
    }

    private String getPassword() {
        try {
            l.lock();
            return password;
        }
        finally {
            l.unlock();
        }
    }

    @Override
    public int hashCode() {
        try {
            l.lock();
            return Objects.hash(getUsername(), password, getPosicao(), isInfetado(), getEncontros(), getTCatual());
        }
        finally {
            l.unlock();
        }
    }

    public void lock() {
        l.lock();
    }

    public void unlock() {
        l.unlock();
    }

    public void setDeveSerNotificado(boolean b) {
        try {
            l.lock();
            deveSerNotificado=b;
        }
        finally {
            l.unlock();
        }
    }

    public boolean isDeveSerNotificado() {
        try {
            l.lock();
            return deveSerNotificado;
        }
        finally {
            l.unlock();
        }
    }
}
