package store.server.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import store.server.user.exception.InvalidUserInfoException;
import store.server.user.exception.UserAuthenticationException;
import store.server.user.exception.UserDeactivatedException;
import store.server.user.exception.UserNotFoundException;

@ControllerAdvice
public class RestControllerAdvice {

    @ExceptionHandler(InvalidUserInfoException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidUserInfoException(InvalidUserInfoException ex) {
        return new ResponseEntity<>(new ExceptionResponse(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAuthenticationException.class)
    public ResponseEntity<ExceptionResponse> handleUserAuthenticationException(UserAuthenticationException ex) {
        return new ResponseEntity<>(new ExceptionResponse(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserDeactivatedException.class)
    public ResponseEntity<ExceptionResponse> handleUserDeactivatedException(UserDeactivatedException ex) {
        return new ResponseEntity<>(new ExceptionResponse(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return new ResponseEntity<>(new ExceptionResponse(ex), HttpStatus.NOT_FOUND);
    }

}
