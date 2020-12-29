package Exceptions;

public class LoggedInException extends Exception{ //Levantada quando o utilizador deveria estar logado e quando não deveria estar logado para aceder à funcionalidade
    String message;

    public LoggedInException(String m) {
        message=m;
    }

    public String getMessage() {
        return message;
    }
}
