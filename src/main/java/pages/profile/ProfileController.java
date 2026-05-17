package pages.profile;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import models.User;
import pages.dashboard.MainController;
import workers.SessionManager;

public class ProfileController {
    @FXML private Label usernameLabel, roleLabel, userIdLabel, devScoreLabel, qaScoreLabel;
    @FXML private Text avatarText;
    @FXML private ComboBox<String> roleComboBox;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("Project Manager", "Developer", "QA"));
        refreshProfile();
    }

    private void refreshProfile() {
        User u = SessionManager.getLoggedUser();
        if (u != null) {
            u.fetch(); // Sync with server
            usernameLabel.setText(u.username);
            roleLabel.setText(u.roleName);
            userIdLabel.setText(u.userId);
            devScoreLabel.setText(String.valueOf(u.devScore));
            qaScoreLabel.setText(String.valueOf(u.qaScore));
            
            String initials = (u.username != null && !u.username.isEmpty()) ? String.valueOf(u.username.charAt(0)).toUpperCase() : "U";
            avatarText.setText(initials);
            
            roleComboBox.setValue(u.roleName);
        }
    }

    @FXML
    private void handleUpdateRole() {
        User u = SessionManager.getLoggedUser();
        String newRole = roleComboBox.getValue();
        if (u != null && newRole != null && !newRole.equals(u.roleName)) {
            u.roleName = newRole;
            u.push();
            
            // Refresh sidebar and navigation
            MainController mc = MainController.getInstance();
            if (mc != null) mc.refreshProfileAndNav();
            
            refreshProfile();
            
            new Alert(Alert.AlertType.INFORMATION, "Role updated to " + newRole + ". Your permissions and Discord mapping have been synchronized.").showAndWait();
        }
    }
}
