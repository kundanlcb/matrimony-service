package com.cm.matrimony_service.biodata;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for {@link Address} entities.
 */
public interface AddressRepository extends JpaRepository<Address, UUID> {
}
