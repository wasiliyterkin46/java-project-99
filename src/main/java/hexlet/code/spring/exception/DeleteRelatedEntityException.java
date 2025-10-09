package hexlet.code.spring.exception;

public class DeleteRelatedEntityException extends RuntimeException {
    public DeleteRelatedEntityException(final String message) {
        super(message);
    }
}
