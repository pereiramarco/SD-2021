package Exceptions;

public class ForaDoMapaException extends Exception{
    String message;
    public ForaDoMapaException(String messageG) {
        message=messageG;
    }

    public String getMessage() {
        return message;
    }

}
