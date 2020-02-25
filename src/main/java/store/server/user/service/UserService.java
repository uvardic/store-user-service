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
        if (emailExists(userRequest))
            throw new InvalidUserInfoException(
                    String.format("Email: %s already exists!", userRequest.getEmail())
            );

        userRequest.setPassword(hashPassword(userRequest.getPassword()));
        userRequest.setActive(true);

        return userRepository.save(userRequest);
    }

    private boolean emailExists(User userRequest) {
        return userRepository.findByEmail(userRequest.getEmail()).isPresent();
    }

    private String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt());
    }

    @Transactional
    public User update(Long existingId, User userRequest) {
        User existingUser = findById(existingId);

        if (emailChanged(existingUser, userRequest) && emailExists(userRequest))
            throw new InvalidUserInfoException(
                    String.format("Email: %s already exists!", userRequest.getEmail())
            );

        return userRepository.save(mapRequest(existingUser, userRequest));
    }

    private boolean emailChanged(User existingUser, User userRequest) {
        return !existingUser.getEmail().equals(userRequest.getEmail());
    }

    private boolean isPasswordPlain(User userRequest) {
        return !BCRYPT_PATTERN.matcher(userRequest.getPassword()).matches();
    }

    private User mapRequest(User existingUser, User userRequest) {
        existingUser.setEmail(userRequest.getEmail());
        existingUser.setLastName(userRequest.getLastName());
        existingUser.setFirstName(userRequest.getFirstName());

        if (isPasswordPlain(userRequest))
            userRequest.setPassword(hashPassword(userRequest.getPassword()));

        return existingUser;
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
        User existingUser = userRepository.findByEmail(tokenRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with email: %s wasn't found!", tokenRequest.getEmail()))
                );

        if (!existingUser.isActive())
            throw new UserDeactivatedException(
                    String.format("Account: %s is deactivated!", existingUser.getEmail())
            );

        if (incorrectPassword(tokenRequest, existingUser))
            throw new UserAuthenticationException("Invalid password!");

        return tokenService.generateTokenFor(existingUser);
    }

    private boolean incorrectPassword(TokenRequest tokenRequest, User existingUser) {
        return BCrypt.checkpw(tokenRequest.getPassword(), existingUser.getPassword());
    }

}
