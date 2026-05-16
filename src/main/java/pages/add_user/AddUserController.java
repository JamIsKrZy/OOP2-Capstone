package pages.add_user;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.User;

public class AddUserController {

    @FXML private TextField txtName, txtEmail; // Treat txtEmail as UserID
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
        String username = txtName.getText() != null ? txtName.getText().trim() : "";
        String userId = txtEmail.getText() != null ? txtEmail.getText().trim() : "";
        String password = txtPassword != null ? txtPassword.getText() : "";
        RadioButton selected = (RadioButton) roleGroup.getSelectedToggle();
        String role = (selected != null) ? selected.getText() : "Developer";

        if (username.isEmpty() || userId.isEmpty() || password.isEmpty()) {
            return;
        }
        if (User.findUserById(userId) != null) {
            return;
        }

        // User(userId, username, roleName, devScore, qaScore)
        User user = new User(userId, username, role, 0, 0);
        
        new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING, 
            "Manual user creation is not yet supported. Please login via Discord.").showAndWait();
        
        handleCancel();
    }
}
