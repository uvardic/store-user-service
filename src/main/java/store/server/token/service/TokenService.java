package store.server.token.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import store.server.token.dto.TokenResponse;
import store.server.user.domain.User;

@Slf4j
@Service
@PropertySource(value = "/security.properties")
public class TokenService {

    private final String tokenSecret;

    public TokenService(@Value("${token.secret}") String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    public TokenResponse generateTokenFor(User user) {
        String token = Jwts.builder()
                .setClaims(createClaims(user))
                .signWith(SignatureAlgorithm.HS512, tokenSecret)
                .compact();

        return new TokenResponse(token);
    }

    private Claims createClaims(User user) {
        Claims claims = Jwts.claims();

        claims.put("id", user.getId());
        claims.put("email", user.getEmail());

        return claims;
    }

}
