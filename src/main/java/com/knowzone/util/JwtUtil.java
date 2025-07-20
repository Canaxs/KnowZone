package com.knowzone.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    private final PrivateKey privateKey;

    public JwtUtil() throws Exception{
        try (InputStream is = new ClassPathResource("keys/private_key.pem").getInputStream()) {
            String key = new String(is.readAllBytes())
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            this.privateKey = kf.generatePrivate(keySpec);
        }
        catch (Exception e) {
            throw new Exception("JWT private key initialization failed: " + e.getMessage() , e);
        }
    }
    public String generateToken(String username, String userId) {
        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        claims.put("username",username);
        claims.put("userId",userId);
        return Jwts.builder()
                .setSubject(username)
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(3600))) // 1 saat geçerli
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }


}
