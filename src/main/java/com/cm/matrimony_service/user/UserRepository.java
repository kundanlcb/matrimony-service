package com.cm.matrimony_service.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findByMobileNumber(String mobileNumber);

	java.util.List<User> findByTestUserTrue();
}
