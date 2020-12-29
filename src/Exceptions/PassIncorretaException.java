package Exceptions;

public class PassIncorretaException extends Exception { //Levantada quando a password inserida não esta correta
    String message;
    public PassIncorretaException(String m) {
        message=m;
    }

    public String getMessage() {
        return message;
    }
}
