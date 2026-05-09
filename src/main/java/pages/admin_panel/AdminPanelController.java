package pages.admin_panel;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User;
import pages.dashboard.MainController;
import workers.MockDataProvider;
import workers.SessionManager;

 public class AdminPanelController {
     @FXML private VBox userListContainer;
     @FXML private TextField userSearchField;

     private String currentSearchQuery = "";

     @FXML
     public void initialize() {
         initializeUserSearch();
         refreshTable();
       }

     private void initializeUserSearch() {
         if (userSearchField != null) {
             userSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                 currentSearchQuery = newVal != null ? newVal.toLowerCase() : "";
                 refreshTable();
               });
           }
       }

     private boolean userMatchesSearch(User u) {
         if (currentSearchQuery.isEmpty()) return true;
         return (u.name != null && u.name.toLowerCase().contains(currentSearchQuery))
                  || (u.email != null && u.email.toLowerCase().contains(currentSearchQuery))
                  || (u.role != null && u.role.toLowerCase().contains(currentSearchQuery))
                  || (u.status != null && u.status.toLowerCase().contains(currentSearchQuery));
       }

     public void refreshTable() {
         userListContainer.getChildren().clear();
         int i = 0;
         List<User> users = MockDataProvider.getUsers();
         // Filter by search query
         if (!currentSearchQuery.isEmpty()) {
             users = users.stream().filter(this::userMatchesSearch).collect(Collectors.toList());
           }
         for (User u : users) {
             HBox row = createUserRow(u);
             if (i % 2 != 0) row.getStyleClass().add("list-row-alt");
             userListContainer.getChildren().add(row);
             i++;
           }
       }

    private HBox createUserRow(User u) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("list-row");

        HBox nameBox = new HBox(12);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        nameBox.setPrefWidth(250);
        StackPane av = new StackPane(new Circle(16, Color.valueOf("#374151")), new Label(u.initials));
        ((Label) av.getChildren().get(1)).setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label nameLabel = new Label(u.name);
        nameLabel.setStyle("-fx-text-fill: #111827; -fx-font-weight: 600;");
        nameBox.getChildren().addAll(av, nameLabel);

        Label email = new Label(u.email);
        email.setPrefWidth(250);
        email.setStyle("-fx-text-fill: #6b7280;");
        Label roleTag = new Label(u.role);
        roleTag.getStyleClass().add("tag");
        roleTag.setStyle("-fx-background-color: #f3e8ff; -fx-text-fill: #9333ea;");
        StackPane rolePane = new StackPane(roleTag);
        rolePane.setPrefWidth(150);
        rolePane.setAlignment(Pos.CENTER_LEFT);
        Label statusTag = new Label(u.displayStatus());
        statusTag.getStyleClass().addAll("status-tag", u.isActive() ? "status-approved" : "status-tag");
        statusTag.setStyle(u.isActive()
                ? ""
                : "-fx-background-color: #fef2f2; -fx-text-fill: #dc2626;");
        StackPane statusPane = new StackPane(statusTag);
        statusPane.setPrefWidth(100);
        statusPane.setAlignment(Pos.CENTER_LEFT);

        Button btnEdit = new Button("Edit Role");
        btnEdit.getStyleClass().addAll("action-btn", "btn-edit");
        btnEdit.setOnAction(e -> editRole(u));

        Button btnDisable = new Button(u.isActive() ? "Disable" : "Enable");
        btnDisable.getStyleClass().addAll("action-btn", "btn-disable");
        btnDisable.setOnAction(e -> toggleDisable(u));

        Button btnDelete = new Button("Delete");
        btnDelete.getStyleClass().addAll("action-btn", "btn-delete");
        btnDelete.setOnAction(e -> deleteUser(u));

        HBox actions = new HBox(12, btnEdit, btnDisable, btnDelete);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPrefWidth(300);

        row.getChildren().addAll(nameBox, email, rolePane, statusPane, actions);
        return row;
    }

    private void editRole(User u) {
        List<String> choices = List.of("Project Manager", "Developer", "QA");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(u.role, choices);
        dialog.setTitle("Edit Role");
        dialog.setHeaderText(u.name);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newRole -> {
            u.role = newRole;
            refreshTable();
            User logged = SessionManager.getLoggedUser();
            if (logged != null && logged.id.equals(u.id)) {
                MainController mc = MainController.getInstance();
                if (mc != null) mc.refreshProfileAndNav();
            }
        });
    }

    private void toggleDisable(User u) {
        User self = SessionManager.getLoggedUser();
        if (self != null && self.id.equals(u.id) && u.isActive()) {
            new Alert(Alert.AlertType.WARNING, "You cannot disable your own account while logged in.").showAndWait();
            return;
        }
        if ("Project Manager".equals(u.role) && u.isActive()
                && MockDataProvider.countUsersWithRole("Project Manager") <= 1) {
            new Alert(Alert.AlertType.WARNING, "Cannot disable the last active Project Manager.").showAndWait();
            return;
        }
        u.status = u.isActive() ? "disabled" : "active";
        refreshTable();
    }

    private void deleteUser(User u) {
        User self = SessionManager.getLoggedUser();
        if (self != null && self.id.equals(u.id)) {
            new Alert(Alert.AlertType.WARNING, "You cannot delete your own account.").showAndWait();
            return;
        }
        if ("Project Manager".equals(u.role) && MockDataProvider.countUsersWithRole("Project Manager") <= 1) {
            new Alert(Alert.AlertType.WARNING, "Cannot delete the last Project Manager.").showAndWait();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete user");
        confirm.setHeaderText("Delete " + u.name + "?");
        confirm.setContentText("This cannot be undone.");
        Optional<ButtonType> ok = confirm.showAndWait();
        if (ok.isPresent() && ok.get() == ButtonType.OK) {
            MockDataProvider.deleteUserById(u.id);
            refreshTable();
        }
    }

    @FXML
    private void openAddUserModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/AddUserModal.fxml"));
            VBox root = loader.load();
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
