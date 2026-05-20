package com.example.midtermproject.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for password hashing using SHA-256.
 */
public class PasswordUtils {

    /**
     * Hashes a password string using SHA-256.
     * @param password Plain text password
     * @return Hex string of the SHA-256 hash
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Verifies a password against a stored hash.
     * @param password Plain text password to verify
     * @param storedHash The stored SHA-256 hash to compare against
     * @return true if the password matches the hash
     */
    public static boolean verifyPassword(String password, String storedHash) {
        String hash = hashPassword(password);
        return hash.equals(storedHash);
    }
}
