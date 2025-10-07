package hexlet.code.spring.exception;

public class DuplicateDataException extends RuntimeException {
    public DuplicateDataException(final String message) {
        super(message);
    }
}
