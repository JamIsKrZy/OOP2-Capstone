package pages.report;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import models.User;

import java.util.List;

public class ProgressReportsController {
    @FXML private VBox reportsContainer;
    @FXML private Button btnDevToggle, btnQAToggle;

    @FXML public void initialize() {
        showDevs(); // Default to Developer view
    }

    @FXML
    private void showDevs() {
        btnDevToggle.getStyleClass().add("active");
        btnQAToggle.getStyleClass().remove("active");
        populateReports("Developer");
    }

    @FXML
    private void showQA() {
        btnQAToggle.getStyleClass().add("active");
        btnDevToggle.getStyleClass().remove("active");
        populateReports("QA");
    }

    private void populateReports(String roleFilter) {
        reportsContainer.getChildren().clear();
        List<User> users = User.getUsers(roleFilter);

        int rank = 1;
        for (User u : users) {
            if (roleFilter.equals(u.roleName)) {
                reportsContainer.getChildren().add(createReportCard(u, roleFilter.equals("Developer"), rank));
                rank++;
            }
        }
    }

    private VBox createReportCard(User u, boolean isDev, int rank) {
        // Main Card Container
        VBox card = new VBox(25);
        card.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        // Top Section (Avatar + Name)
        HBox topSection = new HBox(15);
        topSection.setAlignment(Pos.CENTER_LEFT);

        // Rank Avatar (🥇 for 1, 🥈 for 2, 🥉 for 3, etc.)
        StackPane avatar = new StackPane();
        Circle avatarCircle = new Circle(22, Color.web("#fef3c7"));
        String emoji = "👤";
        if (rank == 1) emoji = "🥇";
        else if (rank == 2) emoji = "🥈";
        else if (rank == 3) emoji = "🥉";
        Label rankEmoji = new Label(emoji);
        rankEmoji.setStyle("-fx-font-size: 18px;");
        avatar.getChildren().addAll(avatarCircle, rankEmoji);

        // Name Container
        VBox infoContainer = new VBox(3);
        Label nameLabel = new Label(u.username);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #111827;");
        infoContainer.getChildren().add(nameLabel);

        topSection.getChildren().addAll(avatar, infoContainer);

        // Stats Section
        HBox statsGrid = new HBox(20);
        statsGrid.setAlignment(Pos.CENTER);

        if (isDev) {
            statsGrid.getChildren().add(createPerfBlock("Dev Score", u.devScore, "#eff6ff", "#2563eb"));
        } else {
            statsGrid.getChildren().add(createPerfBlock("QA Score", u.qaScore, "#f5f3ff", "#7c3aed"));
        }

        card.getChildren().addAll(topSection, statsGrid);
        return card;
    }

    private VBox createPerfBlock(String labelStr, int value, String bgColor, String labelColor) {
        VBox block = new VBox(8);
        block.setAlignment(Pos.CENTER);
        block.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 20; -fx-background-radius: 10;");
        HBox.setHgrow(block, Priority.ALWAYS);

        Label label = new Label(labelStr);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + labelColor + ";");

        Label valLabel = new Label(String.valueOf(value));
        valLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        block.getChildren().addAll(label, valLabel);
        return block;
    }
}