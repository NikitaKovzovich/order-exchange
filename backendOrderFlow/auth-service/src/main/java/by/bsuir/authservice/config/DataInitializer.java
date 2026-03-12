package by.bsuir.authservice.config;

import by.bsuir.authservice.entity.User;
import by.bsuir.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JdbcTemplate jdbcTemplate;

	@Override
	public void run(String... args) {
		migrateEventsPayloadColumn();

		if (userRepository.findByEmail("admin@test.com").isEmpty()) {
			User admin = User.builder()
					.email("admin@test.com")
					.passwordHash(passwordEncoder.encode("password123"))
					.role(User.Role.ADMIN)
					.isActive(true)
					.status("ACTIVE")
					.createdAt(LocalDateTime.now())
					.build();
			userRepository.save(admin);
			log.info("Created admin user: admin@test.com / password123");
		} else {
			User admin = userRepository.findByEmail("admin@test.com").get();
			if (!passwordEncoder.matches("password123", admin.getPasswordHash())) {
				admin.setPasswordHash(passwordEncoder.encode("password123"));
				userRepository.save(admin);
				log.info("Fixed admin password hash for: admin@test.com");
			}
		}

		userRepository.findByEmail("supplier@test.com").ifPresent(user -> {
			if (!passwordEncoder.matches("password123", user.getPasswordHash())) {
				user.setPasswordHash(passwordEncoder.encode("password123"));
				userRepository.save(user);
				log.info("Fixed supplier password hash for: supplier@test.com");
			}
		});

		userRepository.findByEmail("retailchain@test.com").ifPresent(user -> {
			if (!passwordEncoder.matches("password123", user.getPasswordHash())) {
				user.setPasswordHash(passwordEncoder.encode("password123"));
				userRepository.save(user);
				log.info("Fixed retailchain password hash for: retailchain@test.com");
			}
		});
	}

	
	private void migrateEventsPayloadColumn() {
		try {
			String dbUrl = jdbcTemplate.getDataSource() != null
					? jdbcTemplate.getDataSource().getConnection().getMetaData().getURL()
					: "";
			if (!dbUrl.contains("mysql") && !dbUrl.contains("mariadb")) {
				log.debug("Skipping events payload migration: not MySQL (url={})", dbUrl);
				return;
			}
			jdbcTemplate.execute("ALTER TABLE events MODIFY COLUMN payload JSON NOT NULL");
			log.info("✓ Events payload column migrated to JSON");
		} catch (Exception e) {
			log.debug("Events payload column migration skipped: {}", e.getMessage());
		}
	}
}
