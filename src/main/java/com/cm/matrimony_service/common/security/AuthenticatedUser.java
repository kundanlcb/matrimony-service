package com.cm.matrimony_service.common.security;

import java.util.UUID;

public record AuthenticatedUser(UUID id, String mobileNumber, String role) {
}
