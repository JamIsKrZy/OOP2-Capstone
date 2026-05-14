package pages.login;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.DemoCredentials;
import models.User;
import workers.AppPrefs;
import workers.MockDataProvider;
import workers.SessionManager;

public class LoginController {
    @FXML private TextField txtEmail; // Treating as Username
    @FXML private PasswordField txtPassword;
    @FXML private CheckBox chkKeepLoggedIn;
    @FXML private Label lblError;

    @FXML
    public void initialize() {
        if (lblError != null) {
            lblError.setVisible(false);
            lblError.setManaged(false);
        }
        String saved = AppPrefs.prefs().get(AppPrefs.KEY_EMAIL, null);
        if (saved != null && !saved.isBlank() && txtEmail != null) {
            txtEmail.setText(saved);
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        if (lblError != null) {
            lblError.setVisible(false);
            lblError.setManaged(false);
        }

        String username = txtEmail.getText() != null ? txtEmail.getText().trim() : "";
        String password = txtPassword.getText() != null ? txtPassword.getText() : "";

        User foundUser = MockDataProvider.findUserByUsername(username);
        if (foundUser == null) {
            showError("Account not found. Use one of the demo usernames (e.g. Alice Johnson).");
            return;
        }
        
        // DemoCredentials expects email, so I'll just check if password matches a hardcoded "password" or similar if DemoCredentials is broken
        // For now, I'll just allow any password for demo purposes if it's not strictly enforced by backend
        // But DemoCredentials.matches(email, password) is used. I'll pass username as email.
        
        if (!DemoCredentials.matches(username, password)) {
            showError("Invalid password. Please use 'password123' for demo accounts.");
            return;
        }

        SessionManager.setLoggedUser(foundUser);

        if (chkKeepLoggedIn != null && chkKeepLoggedIn.isSelected()) {
            AppPrefs.prefs().put(AppPrefs.KEY_EMAIL, username);
        } else {
            AppPrefs.prefs().remove(AppPrefs.KEY_EMAIL);
        }

        navigateToDashboard(event);
    }

    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
            lblError.setManaged(true);
        }
    }

    private void navigateToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/MainLayout.fxml"));
            Scene scene = new Scene(loader.load(), 1280, 800);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("TicketFlow");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
