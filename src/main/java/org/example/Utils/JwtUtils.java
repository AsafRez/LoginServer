package org.example.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;


@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secretString;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretString.getBytes());
    }

    public int extractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .parseClaimsJws(token)
                    .getBody();

            Integer userId = claims.get("User_id", Integer.class);
            return (userId != null) ? userId : -1; // החזרת ערך שלילי אם ה-ID חסר

        } catch (Exception e) {
            // אם הטוקן פג תוקף או שונה בדרך, נחזיר -1 כדי לסמן שהמשתמש לא מזוהה
            System.err.println("JWT Extraction Error: " + e.getMessage());
            return -1;
        }
    }

    public String generateToken(String username, int userId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("User_id", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // משתמש במפתח מהקובץ
                .compact();
    }
}