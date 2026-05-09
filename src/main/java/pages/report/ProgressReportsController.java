package pages.report;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import models.User;
import workers.MockDataProvider;

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
        List<User> users = MockDataProvider.getUsers();

        int rank = 1;
        for (User u : users) {
            if (u.role.equals(roleFilter)) {
                reportsContainer.getChildren().add(createReportCard(u, roleFilter.equals("Developer"), rank));
                rank++;
            }
        }
    }

    private VBox createReportCard(User u, boolean isDev, int rank) {
        // Main Card Container
        VBox card = new VBox(25);
        card.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        // Top Section (Avatar + Name/Email + Status)
        HBox topSection = new HBox(15);
        topSection.setAlignment(Pos.CENTER_LEFT);

        // Rank Avatar (🥇 for 1, 🥈 for 2)
        StackPane avatar = new StackPane();
        Circle avatarCircle = new Circle(22, Color.web("#fef3c7"));
        Label rankEmoji = new Label(rank == 1 ? "🥇" : "🥈");
        rankEmoji.setStyle("-fx-font-size: 18px;");
        avatar.getChildren().addAll(avatarCircle, rankEmoji);

        // Name and Email Container
        VBox infoContainer = new VBox(3);
        Label nameLabel = new Label(u.name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #111827;"); // DARK BOLD NAME

        Label emailLabel = new Label(u.email);
        emailLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 13px;");
        infoContainer.getChildren().addAll(nameLabel, emailLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusBadge = new Label(u.displayStatus());
        statusBadge.getStyleClass().addAll("status-tag", u.isActive() ? "status-approved" : "status-tag");
        if (!u.isActive()) {
            statusBadge.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #dc2626;");
        }

        topSection.getChildren().addAll(avatar, infoContainer, spacer, statusBadge);

        // Stats Section (The three colored blocks)
        HBox statsGrid = new HBox(20);
        statsGrid.setAlignment(Pos.CENTER);

        if (isDev) {
            statsGrid.getChildren().addAll(
                    createPerfBlock("In Progress", u.inProgress, "#eff6ff", "#2563eb"),
                    createPerfBlock("Resolved", u.resolved, "#f0fdf4", "#16a34a"),
                    createPerfBlock("Closed", u.closed, "#f9fafb", "#4b5563")
            );
        } else {
            statsGrid.getChildren().addAll(
                    createPerfBlock("Reviewed", u.reviewed, "#f5f3ff", "#7c3aed"),
                    createPerfBlock("Approved", u.approved, "#f0fdf4", "#16a34a"),
                    createPerfBlock("Closed", u.closed, "#f9fafb", "#4b5563")
            );
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