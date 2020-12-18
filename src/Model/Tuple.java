package Model;

import java.util.Objects;

public class Tuple<T,U> {
    T first;
    U second;

    public Tuple(T f, U s) {
        first=f;
        second=s;
    }

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    public void setFirst(T f) {
        first = f;
    }

    public void setSecond(U s) {
        second = s;
    }

    @Override
    public boolean equals (Object t) {
        if (t.getClass()!=this.getClass()) return false;
        Tuple<T,U> tt = (Tuple<T, U>)t;
        return this.first==tt.getFirst() && this.second==tt.getSecond();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirst(), getSecond());
    }

    @Override
    public String toString() {
        return "("+first.toString()+","+second.toString()+")";
    }
}