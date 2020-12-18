package Server;

import Model.User;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class Notifier implements Runnable{
    Map<String,User> users;
    Set<String> usersToNotiy;
    String notificacao;
    ReentrantLock l=new ReentrantLock();

    public Notifier(Map<String, User> u, Set<String> utn, String note) {
        users=u;
        usersToNotiy = utn;
        notificacao = note;
    }

    @Override
    public void run() {
        try {
            l.lock();
            for (String idd : usersToNotiy) {
                User us =users.get(idd);
                try {
                    us.getLock(); // adquire o lock do user
                    DataOutputStream doo = us.getdOatual();
                    if (doo != null) {
                        doo.writeUTF(notificacao);
                        doo.flush();
                    }
                }
                finally {
                    us.leaveLock();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            l.unlock();
        }
    }
}
