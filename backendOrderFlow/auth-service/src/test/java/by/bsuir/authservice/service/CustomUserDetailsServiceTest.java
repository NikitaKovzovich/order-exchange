package by.bsuir.authservice.service;

import by.bsuir.authservice.entity.User;
import by.bsuir.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private CustomUserDetailsService userDetailsService;

	private User testUser;

	@BeforeEach
	void setUp() {
		testUser = User.builder()
				.id(1L)
				.email("test@example.com")
				.passwordHash("hashedPassword")
				.role(User.Role.SUPPLIER)
				.isActive(true)
				.build();
	}

	@Nested
	@DisplayName("Load User By Username Tests")
	class LoadUserByUsernameTests {

		@Test
		@DisplayName("Should load user by email")
		void shouldLoadUserByEmail() {
			when(userRepository.findByEmail("test@example.com"))
					.thenReturn(Optional.of(testUser));

			UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

			assertThat(userDetails).isNotNull();
			assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
		}

		@Test
		@DisplayName("Should throw when user not found")
		void shouldThrowWhenUserNotFound() {
			when(userRepository.findByEmail("unknown@example.com"))
					.thenReturn(Optional.empty());

			assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown@example.com"))
					.isInstanceOf(UsernameNotFoundException.class);
		}
	}
}
