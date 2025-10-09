package hexlet.code.spring.exception;

public class RequestDataCannotBeProcessed extends RuntimeException {
    public RequestDataCannotBeProcessed(final String message) {
        super(message);
    }
}
