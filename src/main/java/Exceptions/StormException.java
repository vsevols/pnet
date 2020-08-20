package Exceptions;

public class StormException extends StormThrowable {
    public StormException(Throwable cause) {
        super(cause);
    }


    public StormException(String message) {
        super(message);
    }

    public StormException(Throwable e, Object throwContext) {
        super(e, throwContext);
    }
}


