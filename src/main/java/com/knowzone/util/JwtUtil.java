package com.knowzone.util;

import com.knowzone.config.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
            log.info("Private key loaded successfully from: {}", privateKeyPath);
        }
        catch (Exception e) {
            log.error("Failed to load private key from: {}", privateKeyPath, e);
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
            log.info("Public key loaded successfully from: {}", publicKeyPath);
        }
        catch (Exception e) {
            log.error("Failed to load public key from: {}", publicKeyPath, e);
            throw new Exception("JWT public key initialization failed: " + e.getMessage(), e);
        }
    }
    
    public String generateToken(String username, String userId) {
        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        claims.put("username",username);
        claims.put("userId",userId);
        
        String token = Jwts.builder()
                .setSubject(username)
                .addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(3600))) // 1 saat geçerli
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
        
        log.info("JWT token generated for user: {}", username);
        return token;
    }

    public CustomUserDetails extractAllClaims(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        log.debug("JWT claims extracted for user: {}", claims.getSubject());
        return new CustomUserDetails(claims.get("userId", String.class), claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);

            boolean isValid = !claims.getBody().getExpiration().before(new java.util.Date());
            log.debug("JWT token validation result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
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
