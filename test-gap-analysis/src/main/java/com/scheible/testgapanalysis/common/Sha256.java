package com.scheible.testgapanalysis.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Source:
 * https://github.com/eugenp/tutorials/blob/master/core-java-modules/core-java-security/src/main/java/com/baeldung/hashing/SHA256Hashing.java
 */
public class Sha256 {

	public static String hash(final String input) {
		try {
			final MessageDigest digest = MessageDigest.getInstance("SHA-256");
			final byte[] encodedhash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			return bytesToHex(encodedhash);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static String bytesToHex(final byte[] hash) {
		final StringBuilder hexString = new StringBuilder();
		for (int i = 0; i < hash.length; i++) {
			final String hex = Integer.toHexString(0xff & hash[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}
}
