package workers;

import java.util.prefs.Preferences;

public final class AppPrefs {
    public static final String NODE_PATH = "com/example/bytemeticketflow";
    public static final String KEY_EMAIL = "demo_user_email";

    public static Preferences prefs() {
        return Preferences.userRoot().node(NODE_PATH);
    }

    private AppPrefs() {}
}
