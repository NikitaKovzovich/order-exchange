package by.bsuir.authservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtProvider {
	@Value("${jwt.secret:your-very-secret-key-that-should-be-at-least-256-bits-long-for-hs256}")
	private String secretKey;

	@Value("${jwt.expiration:3600000}")
	private long expirationTime;

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

	public Long getUserIdFromToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
			Claims claims = Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token)
					.getPayload();
			Object userId = claims.get("userId");
			return userId != null ? ((Number) userId).longValue() : null;
		} catch (JwtException | IllegalArgumentException e) {
			return null;
		}
	}

	public Long getCompanyIdFromToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
			Claims claims = Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token)
					.getPayload();
			Object companyId = claims.get("companyId");
			return companyId != null ? ((Number) companyId).longValue() : null;
		} catch (JwtException | IllegalArgumentException e) {
			return null;
		}
	}

	private String createToken(Map<String, Object> claims, String subject) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expirationTime);

		SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

		return Jwts.builder()
				.claims(claims)
				.subject(subject)
				.issuedAt(now)
				.expiration(expiryDate)
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	public String getEmailFromToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
			Claims claims = Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token)
					.getPayload();
			return claims.getSubject();
		} catch (JwtException | IllegalArgumentException e) {
			throw new JwtException("Cannot extract email from JWT");
		}
	}

	public String getRoleFromToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
			Claims claims = Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token)
					.getPayload();
			return (String) claims.get("role");
		} catch (JwtException | IllegalArgumentException e) {
			throw new JwtException("Cannot extract role from JWT");
		}
	}

	public boolean validateToken(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
			Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}
}
