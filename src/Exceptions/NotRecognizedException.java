package Exceptions;

import Server.NotifierEmptyPosition;

public class NotRecognizedException extends Exception { //Levantada quando o comando inserido não é reconhecido
    String message;

    public NotRecognizedException(String m) {
        message = m;
    }

    public String getMessage() {
        return message;
    }
}
