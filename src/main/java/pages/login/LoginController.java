package pages.login;

import Service.APIClient;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.User;
import workers.AppPrefs;
import workers.SessionManager;

import java.awt.Desktop;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoginController {

    @FXML private Label lblError;
    @FXML private Label lblStatus;
    @FXML private TextField txtBaseUrl;
    @FXML private CheckBox chkKeepLoggedIn;

    private ScheduledExecutorService poller;

    @FXML
    public void initialize() {
        // Load saved URL from prefs
        String savedUrl = AppPrefs.prefs().get("BASE_URL", APIClient.getBaseUrl());
        txtBaseUrl.setText(savedUrl);
        APIClient.setBaseUrl(savedUrl);
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        lblError.setVisible(false);
        lblStatus.setText("Contacting Discord...");
        lblStatus.setVisible(true);
        lblStatus.getStyleClass().removeAll("status-error", "status-info");

        String baseUrl = txtBaseUrl.getText().trim();
        if (baseUrl.isEmpty()) {
            showError("Please enter a valid Backend URL.");
            return;
        }

        APIClient.setBaseUrl(baseUrl);
        AppPrefs.prefs().put("BASE_URL", baseUrl); // Pre-save attempt

        new Thread(() -> {
            try {
                // Initial call to get the OAuth URL and sessionId
                String loginJson = APIClient.get("/auth/login");
                if (loginJson == null || loginJson.contains("\"error\"")) {
                    showError("Failed to initiate login. Is the server running at the specified URL?");
                    return;
                }

                String oauthUrl = extract(loginJson, "url");
                String sessionId = extract(loginJson, "sessionId");

                if (oauthUrl.isEmpty() || sessionId.isEmpty()) {
                    showError("Invalid server response. Please try again.");
                    return;
                }

                // Open browser with Linux-friendly fallback
                boolean opened = openUrl(oauthUrl);
                if (opened) {
                    Platform.runLater(() -> {
                        lblStatus.setText("Please authorize in your browser...");
                        lblStatus.getStyleClass().add("status-info");
                    });
                } else {
                    showError("Could not open browser. Please visit:\n" + oauthUrl);
                    return;
                }

                // Start polling with the sessionId
                startPolling(sessionId, (Stage) ((Node) event.getSource()).getScene().getWindow());

            } catch (Exception e) {
                showError("Connection error: " + e.getMessage());
            }
        }).start();
    }

    private boolean openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return true;
            }
        } catch (Exception e) {
            System.err.println("Desktop.browse failed: " + e.getMessage());
        }

        // Fallback for Linux
        try {
            new ProcessBuilder("xdg-open", url).start();
            return true;
        } catch (Exception e) {
            System.err.println("xdg-open failed: " + e.getMessage());
        }

        return false;
    }

    private void startPolling(String sessionId, Stage stage) {
        if (poller != null) poller.shutdownNow();
        poller = Executors.newSingleThreadScheduledExecutor();

        poller.scheduleAtFixedRate(() -> {
            try {
                String statusJson = APIClient.get("/auth/status?sessionId=" + sessionId);
                if (statusJson != null && statusJson.contains("\"authenticated\":true")) {
                    
                    String userId = extract(statusJson, "userId");
                    String username = extract(statusJson, "username");
                    
                    Platform.runLater(() -> {
                        lblStatus.setText("Authenticated! Loading profile...");
                        poller.shutdown();
                        completeLogin(userId, username, sessionId, stage);
                    });
                }
            } catch (Exception e) {
                System.err.println("Polling error: " + e.getMessage());
            }
        }, 2, 2, TimeUnit.SECONDS);
    }

    private void completeLogin(String userId, String username, String sessionId, Stage stage) {
        try {
            // The OAuth callback's Set-Cookie goes to the web browser, not our
            // JavaFX CookieManager. Manually inject the sessionId cookie so that
            // subsequent API calls (e.g. /api/profile) pass session validation.
            APIClient.setSession(sessionId);

            // Fetch profile
            String profileJson = APIClient.get("/profile?id=" + userId);
            if (profileJson != null && !profileJson.contains("\"error\"")) {
                String role = extract(profileJson, "roleName");
                int devScore = Integer.parseInt(extract(profileJson, "devScore"));
                int qaScore = Integer.parseInt(extract(profileJson, "qaScore"));

                User u = new User(userId, username, role, devScore, qaScore);
                SessionManager.setLoggedUser(u);

                if (chkKeepLoggedIn.isSelected()) {
                    AppPrefs.prefs().put(AppPrefs.KEY_EMAIL, userId);
                    AppPrefs.prefs().put("BASE_URL", txtBaseUrl.getText().trim());
                }

                // Transition to Dashboard
                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/MainLayout.fxml"));
                        Scene scene = new Scene(loader.load(), 1280, 800);
                        stage.setTitle("TicketFlow - Dashboard");
                        stage.setScene(scene);
                        stage.setMaximized(true);
                    } catch (Exception e) {
                        showError("Failed to load dashboard: " + e.getMessage());
                    }
                });
            } else {
                showError("Profile data missing.");
            }
        } catch (Exception e) {
            showError("Login finalization failed.");
        }
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            lblStatus.setVisible(false);
            lblError.setText(msg);
            lblError.setVisible(true);
            lblError.setManaged(true);
        });
    }

    private String extract(String json, String key) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"" + key + "\":\\s*\"?([^\",}]*)\"?");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) return m.group(1).replace("\"", "");
        return "";
    }
}
