package utils;

import models.User;

/**
 * Lớp tiện ích quản lý phiên đăng nhập hiện tại của người dùng (Session Manager).
 */
public class SessionManager {
    private static User currentUser;
    private static java.time.LocalDateTime loginTime;
    private static double sessionRevenue = 0.0;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static java.time.LocalDateTime getLoginTime() {
        return loginTime;
    }

    public static void setLoginTime(java.time.LocalDateTime time) {
        loginTime = time;
    }

    public static double getSessionRevenue() {
        return sessionRevenue;
    }

    public static void setSessionRevenue(double revenue) {
        sessionRevenue = revenue;
    }

    public static void addSessionRevenue(double amount) {
        sessionRevenue += amount;
    }

    public static void clearSession() {
        currentUser = null;
        loginTime = null;
        sessionRevenue = 0.0;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }
}
