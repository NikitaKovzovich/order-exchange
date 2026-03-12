package by.bsuir.authservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtProvider {
	@Value("${jwt.secret:your-very-secret-key-that-should-be-at-least-256-bits-long-for-hs256}")
	private String secretKeyString;

	@Value("${jwt.expiration:3600000}")
	private long expirationTime;

	private SecretKey secretKey;

	@PostConstruct
	public void init() {
		this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
	}

	public String generateToken(String email, String role) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("role", role);
		return createToken(claims, email);
	}

	public String generateToken(String email, String role, Long userId, Long companyId) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("role", role);
		if (userId != null) {
			claims.put("userId", userId);
		}
		if (companyId != null) {
			claims.put("companyId", companyId);
		}
		return createToken(claims, email);
	}

	public String getEmailFromToken(String token) {
		return getClaims(token).getSubject();
	}

	public String getRoleFromToken(String token) {
		return (String) getClaims(token).get("role");
	}

	public Long getUserIdFromToken(String token) {
		Object userId = getClaims(token).get("userId");
		return userId != null ? ((Number) userId).longValue() : null;
	}

	public Long getCompanyIdFromToken(String token) {
		Object companyId = getClaims(token).get("companyId");
		return companyId != null ? ((Number) companyId).longValue() : null;
	}

	public boolean validateToken(String token) {
		try {
			getClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	private Claims getClaims(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private String createToken(Map<String, Object> claims, String subject) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expirationTime);

		return Jwts.builder()
				.claims(claims)
				.subject(subject)
				.issuedAt(now)
				.expiration(expiryDate)
				.signWith(secretKey)
				.compact();
	}
}
