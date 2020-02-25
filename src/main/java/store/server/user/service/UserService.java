package store.server.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import store.server.token.dto.TokenRequest;
import store.server.token.dto.TokenResponse;
import store.server.token.service.TokenService;
import store.server.user.domain.User;
import store.server.user.exception.InvalidUserInfoException;
import store.server.user.exception.UserAuthenticationException;
import store.server.user.exception.UserDeactivatedException;
import store.server.user.exception.UserNotFoundException;
import store.server.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[ayb]\\$.{56}$");

    private final UserRepository userRepository;

    private final TokenService tokenService;

    @Transactional
    public void deleteById(Long existingId) {
        User foundUser = findById(existingId);
        foundUser.setActive(false);
        userRepository.save(foundUser);
    }

    @Transactional
    public User save(User userRequest) {
        if (emailExists(userRequest.getEmail()))
            throw new InvalidUserInfoException(
                    String.format("Email: %s already exists!", userRequest.getEmail())
            );

        userRequest.setPassword(hashPassword(userRequest.getPassword()));
        userRequest.setActive(true);

        return userRepository.save(userRequest);
    }

    private String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt());
    }

    private boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public User update(Long existingId, User userRequest) {
        if (userNotFound(existingId))
            throw new UserNotFoundException(String.format("User with id: %d not found!", existingId));

        if (emailChanged(existingId, userRequest.getEmail()) && emailExists(userRequest.getEmail()))
            throw new InvalidUserInfoException(
                    String.format("Email: %s already exists!", userRequest.getEmail())
            );

        if (isPasswordPlain(userRequest.getPassword()))
            userRequest.setPassword(hashPassword(userRequest.getPassword()));

        userRequest.setId(existingId);
        userRequest.setActive(true);

        return userRepository.save(userRequest);
    }

    private boolean userNotFound(Long id) {
        return userRepository.findById(id).isEmpty();
    }

    private boolean emailChanged(Long existingId, String newEmail) {
        return !userRepository.findById(existingId)
                .orElseThrow(IllegalStateException::new)
                .getEmail().equals(newEmail);
    }

    private boolean isPasswordPlain(String password) {
        return !BCRYPT_PATTERN.matcher(password).matches();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with id: %d was't found!", id)
                ));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public TokenResponse login(TokenRequest tokenRequest) {
        User foundUser = userRepository.findByEmail(tokenRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with email: %s wasn't found!", tokenRequest.getEmail()))
                );

        if (!foundUser.isActive())
            throw new UserDeactivatedException(
                    String.format("Account: %s is deactivated!", foundUser.getEmail())
            );

        if (incorrectPassword(tokenRequest.getPassword(), foundUser.getPassword()))
            throw new UserAuthenticationException("Invalid password!");

        return tokenService.generateTokenFor(foundUser);
    }

    private boolean incorrectPassword(String plainText, String password) {
        return BCrypt.checkpw(plainText, password);
    }

}
