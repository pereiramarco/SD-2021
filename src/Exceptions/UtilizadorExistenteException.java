package Exceptions;

public class UtilizadorExistenteException extends Exception{ //Levantada quando se tenta criar um utilizador que já existe ou qunado esse Utilizador não existe e se tenta dar login
    String message;
    public UtilizadorExistenteException(String m) {
        message=m;
    }

    public String getMessage() {
        return message;
    }
}
