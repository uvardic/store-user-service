package store.server.user.exception;

public class UserDeactivatedException extends RuntimeException {

    public UserDeactivatedException(String message) {
        super(message);
    }

}
