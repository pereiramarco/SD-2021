package Server;

import Model.Info;
import Model.Tuple;
import Model.User;
import Utils.Frame;
import Utils.Tag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class NotifierEmptyPosition implements Runnable{
    Tuple<Integer,Integer> pos;
    Info i;
    User u;
    ReentrantLock l=new ReentrantLock();
    Condition c = l.newCondition();

    public NotifierEmptyPosition(Info iG, User uG, Tuple<Integer,Integer> posG ) {
        i=iG;
        u=uG;
        pos = posG;
    }

    @Override
    public void run() {
        try {
            l.lock();
            while (i.getNumOfPeopleOn(pos)!=0){
                c.await();
            }
            u.getLock();
            TaggedConnection dO = u.getTCatual();
            List<byte[]> l = new ArrayList<>();
            l.add(String.valueOf(pos.getFirst()).getBytes()); //adiciona a posição X à lista de informações a enviar
            l.add(String.valueOf(pos.getSecond()).getBytes()); //adiciona a posição Y à lista de informações a enviar
            dO.send(new Frame(Tag.REMINDWHENEMPTY,l));
        }
        catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        finally {
            u.leaveLock();
            l.unlock();
        }
    }

    public void signal() {
        try {
            l.lock();
            c.signal();
        }
        finally {
            l.unlock();
        }
    }
}
