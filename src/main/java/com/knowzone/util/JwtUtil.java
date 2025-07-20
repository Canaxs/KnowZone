package com.knowzone.util;

import com.knowzone.config.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    private final PrivateKey privateKey;

    private final PublicKey publicKey;

    public JwtUtil(@Value("${jwt.private-key-path}") String privateKeyPath,
                   @Value("${jwt.public-key-path}") String publicKeyPath) throws Exception{
        try (InputStream is = new ClassPathResource(privateKeyPath).getInputStream()) {
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
        try (InputStream is = new ClassPathResource(publicKeyPath).getInputStream()) {
            String key = new String(is.readAllBytes())
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            this.publicKey = kf.generatePublic(new X509EncodedKeySpec(decoded));
        }
        catch (Exception e) {
            throw new RuntimeException();
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

    public CustomUserDetails extractAllClaims(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return new CustomUserDetails(claims.get("userId", String.class), claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);

            return !claims.getBody().getExpiration().before(new java.util.Date());
        } catch (Exception e) {
            System.out.println("JWT validation failed: " + e.getMessage());
            return false;
        }
    }

    public String getUsername(Claims claims) {
        return claims.getSubject();
    }

    public String getUserId(Claims claims) {
        return claims.get("userId", String.class);
    }

    public String getRole(Claims claims) {
        return claims.get("role", String.class);
    }


}
