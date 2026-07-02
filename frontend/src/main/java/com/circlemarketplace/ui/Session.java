package com.circlemarketplace.ui;

public class Session {
    private static String token;
    private static String username;
    private static String role;

    public static void set(String jwt, String user, String userRole) {
        token = jwt;
        username = user;
        role = userRole;
    }

    public static String getToken() {
        return token;
    }

    public static String getUsername() {
        return username;
    }

    public static String getRole() {
        return role;
    }

    public static void clear() {
        token = null;
        username = null;
        role = null;
    }

    public static boolean isLoggedIn() {
        return token != null;
    }
}