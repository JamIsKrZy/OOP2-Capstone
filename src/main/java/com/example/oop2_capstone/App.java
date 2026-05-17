package com.example.oop2_capstone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.User;
import workers.AppPrefs;
import workers.SessionManager;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Load saved URL first
        String savedUrl = workers.AppPrefs.prefs().get("BASE_URL", null);
        if (savedUrl != null) {
            Service.APIClient.setBaseUrl(savedUrl);
        }

        String savedEmail = workers.AppPrefs.prefs().get(workers.AppPrefs.KEY_EMAIL, null);
        if (savedEmail != null && !savedEmail.isBlank()) {
            try {
                // Try to fetch profile with the saved cookie
                String profileJson = Service.APIClient.get("/profile?id=" + savedEmail);
                if (profileJson != null && !profileJson.contains("\"error\"")) {
                    // Manual parse
                    String username = extract(profileJson, "username");
                    String role = extract(profileJson, "roleName");
                    int devScore = Integer.parseInt(extract(profileJson, "devScore"));
                    int qaScore = Integer.parseInt(extract(profileJson, "qaScore"));

                    User u = new User(savedEmail, username, role, devScore, qaScore);
                    SessionManager.setLoggedUser(u);
                    
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/MainLayout.fxml"));
                    Scene scene = new Scene(loader.load(), 1280, 800);
                    stage.setTitle("TicketFlow");
                    stage.setScene(scene);
                    stage.setMaximized(true);
                    stage.show();
                    return;
                }
            } catch (Exception e) {
                System.err.println("Session validation failed: " + e.getMessage());
            }
            AppPrefs.prefs().remove(AppPrefs.KEY_EMAIL);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/app/Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        stage.setTitle("TicketFlow - Login");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    private String extract(String json, String key) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"" + key + "\":\\s*\"?([^\",}]*)\"?");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) return m.group(1).replace("\"", "");
        return "";
    }

    public static void main(String[] args) {
        launch();
    }
}