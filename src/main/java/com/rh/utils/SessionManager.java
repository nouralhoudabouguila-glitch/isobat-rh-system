package com.rh.utils;

import com.rh.models.User;

/**
 * Singleton qui garde en mémoire l'utilisateur connecté pendant toute la session.
 * Accessible depuis n'importe quel controller via SessionManager.getInstance().
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }

    public boolean isLoggedIn() { return currentUser != null; }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == User.Role.ADMIN;
    }

    public boolean isRH() {
        return currentUser != null && currentUser.getRole() == User.Role.RESPONSABLE_RH;
    }

    public void logout() { currentUser = null; }
}