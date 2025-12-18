package by.bsuir.authservice.service;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

	private JwtProvider jwtProvider;

	@BeforeEach
	void setUp() {
		jwtProvider = new JwtProvider();
		ReflectionTestUtils.setField(jwtProvider, "secretKey",
				"test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha256");
		ReflectionTestUtils.setField(jwtProvider, "expirationTime", 3600000L);
	}

	@Nested
	@DisplayName("Generate Token Tests")
	class GenerateTokenTests {

		@Test
		@DisplayName("Should generate valid token")
		void shouldGenerateValidToken() {
			String token = jwtProvider.generateToken("test@example.com", "SUPPLIER");

			assertThat(token).isNotNull();
			assertThat(token).isNotEmpty();
			assertThat(token.split("\\.")).hasSize(3);
		}

		@Test
		@DisplayName("Should generate different tokens for different users")
		void shouldGenerateDifferentTokensForDifferentUsers() {
			String token1 = jwtProvider.generateToken("user1@example.com", "SUPPLIER");
			String token2 = jwtProvider.generateToken("user2@example.com", "RETAIL_CHAIN");

			assertThat(token1).isNotEqualTo(token2);
		}
	}

	@Nested
	@DisplayName("Extract Email Tests")
	class ExtractEmailTests {

		@Test
		@DisplayName("Should extract email from token")
		void shouldExtractEmailFromToken() {
			String email = "test@example.com";
			String token = jwtProvider.generateToken(email, "SUPPLIER");

			String extractedEmail = jwtProvider.getEmailFromToken(token);

			assertThat(extractedEmail).isEqualTo(email);
		}

		@Test
		@DisplayName("Should throw exception for invalid token")
		void shouldThrowExceptionForInvalidToken() {
			assertThatThrownBy(() -> jwtProvider.getEmailFromToken("invalid.token.here"))
					.isInstanceOf(JwtException.class);
		}
	}

	@Nested
	@DisplayName("Extract Role Tests")
	class ExtractRoleTests {

		@Test
		@DisplayName("Should extract role from token")
		void shouldExtractRoleFromToken() {
			String role = "SUPPLIER";
			String token = jwtProvider.generateToken("test@example.com", role);

			String extractedRole = jwtProvider.getRoleFromToken(token);

			assertThat(extractedRole).isEqualTo(role);
		}

		@Test
		@DisplayName("Should throw exception for invalid token")
		void shouldThrowExceptionForInvalidTokenRole() {
			assertThatThrownBy(() -> jwtProvider.getRoleFromToken("invalid.token"))
					.isInstanceOf(JwtException.class);
		}
	}

	@Nested
	@DisplayName("Validate Token Tests")
	class ValidateTokenTests {

		@Test
		@DisplayName("Should validate correct token")
		void shouldValidateCorrectToken() {
			String token = jwtProvider.generateToken("test@example.com", "SUPPLIER");

			boolean isValid = jwtProvider.validateToken(token);

			assertThat(isValid).isTrue();
		}

		@Test
		@DisplayName("Should return false for invalid token")
		void shouldReturnFalseForInvalidToken() {
			boolean isValid = jwtProvider.validateToken("invalid.token.here");

			assertThat(isValid).isFalse();
		}

		@Test
		@DisplayName("Should return false for null token")
		void shouldReturnFalseForNullToken() {
			boolean isValid = jwtProvider.validateToken(null);

			assertThat(isValid).isFalse();
		}

		@Test
		@DisplayName("Should return false for empty token")
		void shouldReturnFalseForEmptyToken() {
			boolean isValid = jwtProvider.validateToken("");

			assertThat(isValid).isFalse();
		}
	}
}
