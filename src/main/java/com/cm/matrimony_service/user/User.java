package com.cm.matrimony_service.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a user in the system.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

	@Id
	private UUID id;



	@Column(name = "is_verified", nullable = false)
	private boolean verified;

	@Column(name = "password")
	private String password;

	@Column(name = "is_test_user", nullable = false)
	private boolean testUser = false;

	@Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
	private boolean active = true;

	@Column(name = "is_hidden", nullable = false, columnDefinition = "boolean default false")
	private boolean hidden = false;

	@Column(name = "email", nullable = false, unique = true, length = 100)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "registration_step", nullable = false, length = 20)
	private RegistrationStep registrationStep = RegistrationStep.AUTH;

	@Enumerated(EnumType.STRING)
	@Column(name = "preferred_language", nullable = false, length = 5)
	private PreferredLanguage preferredLanguage = PreferredLanguage.EN;

	@Enumerated(EnumType.STRING)
	@Column(name = "theme_preference", nullable = false, length = 10)
	private ThemePreference themePreference = ThemePreference.LIGHT;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public User(String email) {
		this.email = email;
	}

	@PrePersist
	void prePersist() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		if (email != null && email.startsWith("test")) {
			this.testUser = true;
		}
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = Instant.now();
	}
}
