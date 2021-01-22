package Server;

import Utils.Frame;
import Utils.Tag;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable {
    Socket socket;
    ReentrantLock rlock = new ReentrantLock();
    ReentrantLock wlock = new ReentrantLock();
    DataOutputStream dO;
    DataInputStream dI;

    public TaggedConnection(Socket socket) throws IOException {
        this.socket=socket;
        dO = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        dI = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    public void send(Frame frame) throws IOException {
        try {
            wlock.lock();
            send(frame.tag, frame.data);
        }
        finally {
            wlock.unlock();
        }
    }

    public void send(Tag tag, List<byte[]> data) throws IOException {
        int i=0,s;
        try {
            wlock.lock();
            dO.writeUTF(tag.name());
            s=data==null ? 0 : data.size();
            dO.writeInt(s);
            while (i<s) { //escreve cada argumento na lista
                dO.writeInt(data.get(i).length);
                dO.write(data.get(i));
                i++;
            }
            dO.flush();
        }
        finally {
            wlock.unlock();
        }
    }

    public Frame receive() throws IOException {
        Tag t;
        List<byte[]> b;
        int toRead;
        try {
            rlock.lock();
            t = Tag.valueOf(dI.readUTF());
            toRead=dI.readInt();
            b = new ArrayList<>();
            while (toRead>0) { //lê cada argumento na lista
                int size = dI.readInt();
                byte[] n = new byte[size];
                dI.readFully(n);
                b.add(n);
                toRead--;
            }
        }
        finally {
            rlock.unlock();
        }
        return new Frame(t,b);
    }

    public void close() throws IOException {
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
    }

    public boolean isConnected() {
        return socket.isConnected();
    }
}