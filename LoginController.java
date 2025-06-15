package com.campuscent;

import com.campuscent.utils.AuthenticationHelper;

public class LoginController {
    private DatabaseHelper dbHelper;

    public LoginController(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Login method
    public String login(String username, String plainPassword) {
        User user = dbHelper.getUserByUsername(username);
        if (user != null && AuthenticationHelper.verifyPassword(plainPassword, user.getHashedPassword())) {
            return SessionManager.createSession(username); // Return session ID
        }
        return null; // Authentication failed
    }

    // Logout method
    public void logout(String sessionId) {
        SessionManager.invalidateSession(sessionId);
    }
}
