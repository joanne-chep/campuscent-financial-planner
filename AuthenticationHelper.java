package com.campuscent.utils;
import org.mindrot.jbcrypt.BCrypt;

public class AuthenticationHelper {
    // Method to hash a password using BCrypt
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Method to verify a password against a hashed password
    public static boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}
