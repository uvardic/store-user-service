package store.server.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import store.server.exception.InvalidUserInfoException;
import store.server.exception.UserNotFoundException;
import store.server.user.domain.User;
import store.server.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[ayb]\\$.{56}$");

    private final UserRepository userRepository;

    @Transactional
    public void deleteById(Long existingId) {
        if (userNotFound(existingId))
            throw new UserNotFoundException(String.format("User with id: %d not found!", existingId));

        userRepository.deleteById(existingId);
    }

    private boolean userNotFound(Long id) {
        return userRepository.findById(id).isEmpty();
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

    private boolean emailChanged(Long existingId, String newEmail) {
        return !userRepository.findById(existingId)
                .orElseThrow(IllegalStateException::new)
                .getEmail().equals(newEmail);
    }

    private boolean isPasswordPlain(String password) {
        return BCRYPT_PATTERN.matcher(password).matches();
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

}