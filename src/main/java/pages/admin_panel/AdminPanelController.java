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
import workers.SessionManager;

 public class AdminPanelController {
     @FXML private VBox userListContainer;
     @FXML private TextField userSearchField;
     @FXML private Label totalUsersLabel, activeUsersLabel, activeTicketsLabel, closedTicketsLabel;

     private String currentSearchQuery = "";

     @FXML
     public void initialize() {
         initializeUserSearch();
         refreshTable();
         refreshStats();
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
         return (u.username != null && u.username.toLowerCase().contains(currentSearchQuery))
                   || (u.roleName != null && u.roleName.toLowerCase().contains(currentSearchQuery));
       }

    public void refreshTable() {
        userListContainer.getChildren().clear();
        int i = 0;
        List<User> users = User.getUsers("all");
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
        
        String initials = (u.username != null && !u.username.isEmpty()) ? String.valueOf(u.username.charAt(0)).toUpperCase() : "U";
        StackPane av = new StackPane(new Circle(16, Color.valueOf("#374151")), new Label(initials));
        ((Label) av.getChildren().get(1)).setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label nameLabel = new Label(u.username);
        nameLabel.setStyle("-fx-text-fill: #111827; -fx-font-weight: 600;");
        nameBox.getChildren().addAll(av, nameLabel);

        Label userIdLabel = new Label("ID: " + u.userId);
        userIdLabel.setPrefWidth(250);
        userIdLabel.setStyle("-fx-text-fill: #6b7280;");
        
        Label roleTag = new Label(u.roleName);
        roleTag.getStyleClass().add("tag");
        roleTag.setStyle("-fx-background-color: #f3e8ff; -fx-text-fill: #9333ea;");
        StackPane rolePane = new StackPane(roleTag);
        rolePane.setPrefWidth(150);
        rolePane.setAlignment(Pos.CENTER_LEFT);

        Button btnEdit = new Button("Edit Role");
        btnEdit.getStyleClass().addAll("action-btn", "btn-edit");
        btnEdit.setOnAction(e -> editRole(u));

        Button btnDelete = new Button("Delete");
        btnDelete.getStyleClass().addAll("action-btn", "btn-delete");
        btnDelete.setOnAction(e -> deleteUser(u));

        HBox actions = new HBox(12, btnEdit, btnDelete);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPrefWidth(300);

        row.getChildren().addAll(nameBox, userIdLabel, rolePane, actions);
        return row;
    }

    private void refreshStats() {
        try {
            String json = Service.APIClient.get("/stats");
            com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
            
            if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(node.get("totalUsers").asInt()));
            if (activeUsersLabel != null) activeUsersLabel.setText(String.valueOf(node.get("activeUsers").asInt()));
            if (activeTicketsLabel != null) activeTicketsLabel.setText(String.valueOf(node.get("activeTickets").asInt()));
            if (closedTicketsLabel != null) closedTicketsLabel.setText(String.valueOf(node.get("closedTickets").asInt()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void editRole(User u) {
        List<String> choices = List.of("Project Manager", "Developer", "QA");
        ChoiceDialog<String> dialog = new ChoiceDialog<>(u.roleName, choices);
        dialog.setTitle("Edit Role");
        dialog.setHeaderText(u.username);
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newRole -> {
            u.roleName = newRole;
            u.push();
            User logged = SessionManager.getLoggedUser();
            if (logged != null && logged.userId.equals(u.userId)) {
                logged.roleName = newRole;
                MainController mc = MainController.getInstance();
                if (mc != null) mc.refreshProfileAndNav();
            }
            refreshTable();
        });
    }

    private void deleteUser(User u) {
        User self = SessionManager.getLoggedUser();
        if (self != null && self.userId.equals(u.userId)) {
            new Alert(Alert.AlertType.WARNING, "You cannot delete your own account.").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete user");
        confirm.setHeaderText("Delete " + u.username + "?");
        confirm.setContentText("This cannot be undone.");
        Optional<ButtonType> ok = confirm.showAndWait();
        if (ok.isPresent() && ok.get() == ButtonType.OK) {
            try {
                Service.APIClient.delete("/user/" + u.userId);
                refreshTable();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Failed to delete user: " + e.getMessage()).showAndWait();
            }
        }
    }

    @FXML
    private void handleLoadTickets() {
        try {
            // Fetch available folders from backend
            String jsonFolders = Service.APIClient.get("/tickets/folders");
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<String> folders = mapper.readValue(jsonFolders, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});

            if (folders.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "No ticket folders found in the configured directory.").showAndWait();
                return;
            }

            ChoiceDialog<String> folderDialog = new ChoiceDialog<>(folders.get(0), folders);
            folderDialog.setTitle("Load Tickets");
            folderDialog.setHeaderText("Select folder containing ticket Markdown files");
            folderDialog.setContentText("Folder:");
            
            folderDialog.showAndWait().ifPresent(folder -> {
                TextInputDialog channelDialog = new TextInputDialog("1331252119041212448"); // Default or empty
                channelDialog.setTitle("Target Channel");
                channelDialog.setHeaderText("Specify Discord Text Channel ID");
                channelDialog.setContentText("Channel ID:");
                
                channelDialog.showAndWait().ifPresent(channelId -> {
                    try {
                        String jsonBody = String.format("{\"folder\": \"%s\", \"channelId\": \"%s\"}", folder, channelId);
                        String response = Service.APIClient.post("/tickets/load", jsonBody);
                        new Alert(Alert.AlertType.INFORMATION, response).showAndWait();
                    } catch (Exception e) {
                        new Alert(Alert.AlertType.ERROR, "Failed to load tickets: " + e.getMessage()).showAndWait();
                    }
                });
            });
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to fetch folders: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void handleRebuildDatabase() {
        TextInputDialog channelDialog = new TextInputDialog();
        channelDialog.setTitle("Rebuild Database");
        channelDialog.setHeaderText("Specify Discord Text Channel ID to scan");
        channelDialog.showAndWait().ifPresent(channelId -> {
            try {
                String jsonBody = String.format("{\"channelId\": %s}", channelId);
                String response = Service.APIClient.post("/tickets/rebuild", jsonBody);
                new Alert(Alert.AlertType.INFORMATION, response).showAndWait();
                refreshTable();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Failed to rebuild database: " + e.getMessage()).showAndWait();
            }
        });
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
