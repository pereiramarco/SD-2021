package Model;

import java.io.DataOutputStream;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class User {
    String username;
    String password;
    Tuple<Integer,Integer> posicao;
    boolean infetado;
    HashSet<String> encontros; //users com quem se encontrou
    DataOutputStream dOatual; //DataOutputStream  do user caso esteja logado senão será null
    ReentrantLock l = new ReentrantLock();

    public User(String username,String password,boolean infetado) {
        this.username=username;
        this.password=password;
        this.posicao= null;
        this.infetado=infetado;
        this.encontros = new HashSet<>();
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

    public void setdOatual(DataOutputStream dO) {
        try {
            l.lock();
            dOatual=dO;
        }
        finally {
            l.unlock();
        }
    }

    public DataOutputStream getdOatual() {
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
            return Objects.hash(getUsername(), password, getPosicao(), isInfetado(), getEncontros(), getdOatual());
        }
        finally {
            l.unlock();
        }
    }

    public void getLock() {
        l.lock();
    }

    public void leaveLock() {
        l.unlock();
    }
}
