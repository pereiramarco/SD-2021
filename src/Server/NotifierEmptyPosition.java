package Server;

import Model.Info;
import Model.InfoPosicao;
import Utils.Tuple;
import Model.User;
import Utils.Frame;
import Utils.Tag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NotifierEmptyPosition implements Runnable{
    Tuple<Integer,Integer> coord;
    Info i;
    User u;

    public NotifierEmptyPosition(Info iG,String username, Tuple<Integer,Integer> posG) {
        i=iG;
        u= iG.getUser(username);
        coord = posG;
    }

    @Override
    public void run() {
        InfoPosicao infoPosicao = i.getPosicao(coord);
        try {
            while (i.getNumOfPeopleOn(coord)!=0 || u.getTCatual()==null){
                infoPosicao.await();
            }
            u.lock();
            TaggedConnection tc = u.getTCatual();
            List<byte[]> data = new ArrayList<>();
            data.add(String.valueOf(coord.getFirst()).getBytes()); //adiciona a posição X à lista de informações a enviar
            data.add(String.valueOf(coord.getSecond()).getBytes()); //adiciona a posição Y à lista de informações a enviar
            tc.send(new Frame(Tag.REMINDWHENEMPTY,data));
        }
        catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            u.unlock();
        }
    }
}
