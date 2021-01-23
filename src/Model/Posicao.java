package Model;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Posicao {
    private Set<String> usersNaPosicao;
    private Set<String> historico;
    private ReentrantLock l;
    private Condition c;

    public Posicao() {
        usersNaPosicao = new HashSet<>();
        historico = new HashSet<>();
        l = new ReentrantLock();
        c = l.newCondition();
    }

    public void addUser(String username) {
        try {
            l.lock();
            usersNaPosicao.add(username);
            historico.add(username);
        } finally {
            l.unlock();
        }
    }

    public void removeUser(String username) {
        try {
            l.lock();
            usersNaPosicao.remove(username);
        } finally {
            l.unlock();
        }
    }

    public int nmrPeopleOn() {
        try {
            l.lock();
            return usersNaPosicao.size();
        } finally {
            l.unlock();
        }
    }

    public Set<String> getUsersPosAtual() {
        try {
            l.lock();
            return new HashSet<>(usersNaPosicao);
        } finally {
            l.unlock();
        }
    }

    public Set<String> getHistorico() {
        try {
            l.lock();
            return new HashSet<>(historico);
        } finally {
            l.unlock();
        }
    }

    public void await() throws InterruptedException {
        try {
            l.lock();
            c.await();
        } finally {
            l.unlock();
        }
    }

    public void signallAll() {
        try {
            l.lock();
            c.signalAll();
        } finally {
            l.unlock();
        }
    }
}
