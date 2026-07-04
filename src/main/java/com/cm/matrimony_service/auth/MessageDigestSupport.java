package com.cm.matrimony_service.auth;

import java.security.MessageDigest;

/**
 * Utility class providing support for message digest operations.
 */
final class MessageDigestSupport {

	private MessageDigestSupport() {
	}

	static boolean equals(byte[] left, byte[] right) {
		return MessageDigest.isEqual(left, right);
	}
}
