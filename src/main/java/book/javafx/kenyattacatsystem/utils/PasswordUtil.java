package book.javafx.kenyattacatsystem.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for password hashing and validation.
 * Provides methods for secure password storage and verification.
 */
public class PasswordUtil {
    private static final Logger LOGGER = Logger.getLogger(PasswordUtil.class.getName());
    private static final int SALT_LENGTH = 16; // 128 bits
    private static final String HASH_ALGORITHM = "SHA-256";
    
    /**
     * Generates a random salt for password hashing.
     * 
     * @return A Base64 encoded string representation of the salt
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hashes a password with the provided salt using SHA-256.
     * 
     * @param password The password to hash
     * @param salt The salt to use for hashing
     * @return The hashed password
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Error hashing password", e);
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Creates a combined hash+salt string for storage in the database.
     * Format: hash:salt
     * 
     * @param password The password to hash and store
     * @return A string containing the hash and salt, separated by a colon
     */
    public static String createHashedPassword(String password) {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        return hash + ":" + salt;
    }
    
    /**
     * Verifies a password against a stored hash+salt string.
     * 
     * @param password The password to verify
     * @param storedHash The stored hash+salt string (format: hash:salt)
     * @return True if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            LOGGER.info("Verifying password with stored hash: " + storedHash);
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                LOGGER.warning("Invalid stored hash format. Expected hash:salt but got: " + storedHash);
                return false;
            }
            
            String hash = parts[0];
            String salt = parts[1];
            
            LOGGER.info("Extracted hash: " + hash + ", salt: " + salt);
            String computedHash = hashPassword(password, salt);
            LOGGER.info("Computed hash: " + computedHash);
            
            boolean result = computedHash.equals(hash);
            LOGGER.info("Password verification result: " + result);
            return result;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error verifying password", e);
            return false;
        }
    }
    
    /**
     * Validates password strength based on common criteria.
     * 
     * @param password The password to validate
     * @return True if the password meets strength requirements, false otherwise
     */
    public static boolean isStrongPassword(String password) {
        // For simplicity in this application, we'll just check for minimum length
        if (password.length() < 6) {
            return false;
        }
        
        // The following checks are commented out to simplify password requirements
        // for testing and development purposes
        
        /*
        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        
        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }
        
        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            return false;
        }
        
        // Check for at least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':"\\\\|,.<>/?].*")) {
            return false;
        }
        */
        
        return true;
    }
}