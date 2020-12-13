package Server;

import Model.User;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Notifier implements Runnable{
    Map<String,User> users;
    List<String> usersToNotiy;
    String notificacao;

    public Notifier(Map<String, User> u, List<String> utn, String note) {
        users=u;
        usersToNotiy = utn;
        notificacao = note;
    }

    @Override
    public void run() {
        try {
            for (String idd : usersToNotiy) {
                DataOutputStream doo = users.get(idd).getdOatual();
                if (doo != null) {
                    doo.writeUTF(notificacao);
                    doo.flush();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
