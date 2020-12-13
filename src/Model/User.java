package Model;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class User {
    String username;
    String password;
    Tuple<Integer,Integer> posicao;
    boolean infetado;
    HashSet<String> encontros; //users com quem se encontrou
    DataOutputStream dOatual; //DataOutputStream  do user caso esteja logado senão será null

    public User(String username,String password,boolean infetado) {
        this.username=username;
        this.password=password;
        this.posicao= null;
        this.infetado=infetado;
        this.encontros = new HashSet<>();
    }

    public boolean passwordCorreta(String pass) {
        return pass.equals(password);
    }

    public boolean isInfetado() {
        return infetado;
    }

    public void setInfetado() {
        this.infetado = true;
    }

    public Tuple<Integer, Integer> getPosicao() {
        return posicao;
    }

    public void setPosicao(Tuple<Integer, Integer> posicao) {
        this.posicao = posicao;
    }

    public List<String> getEncontros() {
        return new ArrayList<>(encontros);
    }

    public void addEncontro(String userID) {
        this.encontros.add(userID);
    }

    public void setdOatual(DataOutputStream dO) {
        dOatual=dO;
    }

    public DataOutputStream getdOatual() {
        return dOatual;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.getUsername()) && password.equals(user.getPassword());
    }

    private String getPassword() {
        return password;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsername(), password, getPosicao(), isInfetado(), getEncontros(), getdOatual());
    }
}
