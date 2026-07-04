package com.cm.matrimony_service.common.security;

import java.util.UUID;

/**
 * Represents an authenticated user's details extracted from the security context.
 */
public record AuthenticatedUser(UUID id, String email, String role) {
}
