package models;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Demo passwords aligned with updated usernames; extra registrants from Add User. */
public final class DemoCredentials {

    private static final Map<String, String> NAME_TO_PASSWORD = Map.of(
            "Alice Johnson", "pm12345",
            "Bob Smith", "dev12345",
            "Carol Williams", "qa12345",
            "David Brown", "dev54321",
            "Emma Davis", "qa54321"
    );

    private static final Map<String, String> EXTRA = new ConcurrentHashMap<>();

    private DemoCredentials() {}

    public static void registerPassword(String name, String password) {
        if (name == null || password == null) return;
        EXTRA.put(name.trim().toLowerCase(Locale.ROOT), password);
    }

    public static boolean matches(String name, String password) {
        if (name == null || password == null) return false;
        String key = name.trim().toLowerCase(Locale.ROOT);
        
        // Check case-insensitive for demo convenience
        for (Map.Entry<String, String> entry : NAME_TO_PASSWORD.entrySet()) {
            if (entry.getKey().toLowerCase(Locale.ROOT).equals(key)) {
                return entry.getValue().equals(password);
            }
        }
        
        String expected = EXTRA.get(key);
        return expected != null && expected.equals(password);
    }
}
