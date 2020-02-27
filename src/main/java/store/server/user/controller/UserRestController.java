package store.server.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import store.server.token.dto.TokenRequest;
import store.server.token.dto.TokenResponse;
import store.server.user.domain.User;
import store.server.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/store/server/user")
public class UserRestController {

    private final UserService userService;

    @DeleteMapping("/delete/existingId={existingId}")
    public ResponseEntity<?> delete(@PathVariable Long existingId) {
        userService.deleteById(existingId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/save")
    public ResponseEntity<?> save(@Valid @RequestBody User userRequest) {
        return new ResponseEntity<>(userService.save(userRequest), HttpStatus.CREATED);
    }

    @PutMapping("/update/existingId={existingId}")
    public ResponseEntity<?> update(@PathVariable Long existingId, @Valid @RequestBody User userRequest) {
        return new ResponseEntity<>(userService.update(existingId, userRequest), HttpStatus.OK);
    }

    @GetMapping("/id={id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        return new ResponseEntity<>(userService.findById(id), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> findAll() {
        return new ResponseEntity<>(userService.findAll(), HttpStatus.OK);
    }

    @PutMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody TokenRequest tokenRequest) {
        return new ResponseEntity<>(userService.login(tokenRequest), HttpStatus.OK);
    }

}
