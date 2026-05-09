package models;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Demo passwords aligned with reference Ticketflow mockData.ts; extra registrants from Add User. */
public final class DemoCredentials {

    private static final Map<String, String> EMAIL_TO_PASSWORD = Map.of(
            "alice@example.com", "pm12345",
            "bob@example.com", "dev12345",
            "carol@example.com", "qa12345",
            "david@example.com", "dev54321",
            "emma@example.com", "qa54321"
    );

    private static final Map<String, String> EXTRA = new ConcurrentHashMap<>();

    private DemoCredentials() {}

    public static void registerPassword(String email, String password) {
        if (email == null || password == null) return;
        EXTRA.put(email.trim().toLowerCase(Locale.ROOT), password);
    }

    public static boolean matches(String email, String password) {
        if (email == null || password == null) return false;
        String key = email.trim().toLowerCase(Locale.ROOT);
        String expected = EMAIL_TO_PASSWORD.get(key);
        if (expected == null) {
            expected = EXTRA.get(key);
        }
        return expected != null && expected.equals(password);
    }
}
