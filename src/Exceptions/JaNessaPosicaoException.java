package Exceptions;

public class JaNessaPosicaoException extends Exception { //Levantado quando o utilizador se encontra na posição para a qual se quer mover
    String message;
    public JaNessaPosicaoException(String messageG) {
        message=messageG;
    }

    public String getMessage() {
        return message;
    }
}
