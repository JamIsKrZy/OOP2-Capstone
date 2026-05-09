package pages.add_user;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.DemoCredentials;
import models.User;
import workers.MockDataProvider;

public class AddUserController {

    @FXML private TextField txtName, txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private RadioButton rbPM, rbDev, rbQA;
    @FXML private ToggleGroup roleGroup;
    @FXML private HBox boxPM, boxDev, boxQA;

    @FXML
    public void initialize() {
        roleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> updateRoleStyles());
    }

    private void updateRoleStyles() {
        boxPM.getStyleClass().remove("role-option-selected");
        boxDev.getStyleClass().remove("role-option-selected");
        boxQA.getStyleClass().remove("role-option-selected");

        if (roleGroup.getSelectedToggle() == rbPM) {
            boxPM.getStyleClass().add("role-option-selected");
        } else if (roleGroup.getSelectedToggle() == rbDev) {
            boxDev.getStyleClass().add("role-option-selected");
        } else if (roleGroup.getSelectedToggle() == rbQA) {
            boxQA.getStyleClass().add("role-option-selected");
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCreate() {
        String name = txtName.getText() != null ? txtName.getText().trim() : "";
        String email = txtEmail.getText() != null ? txtEmail.getText().trim().toLowerCase() : "";
        String password = txtPassword != null ? txtPassword.getText() : "";
        RadioButton selected = (RadioButton) roleGroup.getSelectedToggle();
        String role = (selected != null) ? selected.getText() : "Developer";

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return;
        }
        if (MockDataProvider.findUserByEmail(email) != null) {
            return;
        }

        String id = String.valueOf(System.currentTimeMillis());
        String initials = initialsFromName(name);

        User user = new User(id, name, email, role, "active", initials, "#6366f1", "",
                0, 0, 0, 0, 0);
        DemoCredentials.registerPassword(email, password);
        MockDataProvider.addUser(user);
        handleCancel();
    }

    private static String initialsFromName(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        if (name.length() >= 2) {
            return name.substring(0, 2).toUpperCase();
        }
        return name.toUpperCase();
    }
}
