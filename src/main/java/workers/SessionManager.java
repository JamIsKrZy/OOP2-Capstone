package workers;

import models.User;

public class SessionManager {
    private static User loggedUser;

    public static void setLoggedUser(User user) { loggedUser = user; }
    public static User getLoggedUser() { return loggedUser; }
    public static void cleanUserSession() { loggedUser = null; }
}