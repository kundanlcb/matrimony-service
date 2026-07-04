package com.cm.matrimony_service.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing User entities.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

	/**
	 * Finds a user by their email address.
	 *
	 * @param email the email address to search for
	 * @return an Optional containing the user if found, or empty otherwise
	 */
	Optional<User> findByEmail(String email);

	/**
	 * Finds all users that are marked as test users.
	 *
	 * @return a list of test users
	 */
	List<User> findByTestUserTrue();
}
