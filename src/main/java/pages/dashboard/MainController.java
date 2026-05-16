package pages.dashboard;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.User;
import pages.notification_popup.NotificationPopupController;
import workers.AppPrefs;
import workers.SessionManager;
import workers.ViewContext;

public class MainController {
    private static MainController instance;

    public static MainController getInstance() {
        return instance;
    }

    @FXML private Button btnNavAvailable, btnNavReports, btnNavAdmin;
    @FXML private VBox adminReportsSection, adminSystemSection;
    @FXML private StackPane contentArea;
    @FXML private Label lblBreadcrumbCurrent, lblNotificationCount, sidebarUserName, sidebarUserRole, topbarUserName, topbarUserRole;
    @FXML private Text sidebarAvatarText;

    @FXML
    public void initialize() {
        instance = this;
        if (SessionManager.getLoggedUser() == null) {
            Platform.runLater(this::redirectToLogin);
            return;
        }
        setupUserSession();
        applyRoleBasedAccess();
        ViewContext.ticketMode = ViewContext.TicketViewMode.AVAILABLE;
        navAvailableTickets();
    }

    private void redirectToLogin() {
        try {
            Node anchor = contentArea != null ? contentArea : btnNavAvailable;
            if (anchor == null || anchor.getScene() == null) return;
            Stage stage = (Stage) anchor.getScene().getWindow();
            Scene loginScene = new Scene(new FXMLLoader(getClass().getResource("/app/Login.fxml")).load(), 1200, 800);
            stage.setScene(loginScene);
            stage.setTitle("TicketFlow - Login");
            stage.centerOnScreen();
            stage.setMaximized(false);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

    public void setView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
            FadeTransition ft = new FadeTransition(Duration.millis(300), view);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupUserSession() {
        User u = SessionManager.getLoggedUser();
        if (u != null) {
            sidebarUserName.setText(u.username);
            topbarUserName.setText(u.username);
            sidebarUserRole.setText(u.roleName);
            topbarUserRole.setText(u.roleName);
            String initials = (u.username != null && !u.username.isEmpty()) ? String.valueOf(u.username.charAt(0)).toUpperCase() : "U";
            sidebarAvatarText.setText(initials);
        }
    }

    /** Call after admin edits the logged-in user's role so sidebar and RBAC update. */
    public void refreshProfileAndNav() {
        setupUserSession();
        applyRoleBasedAccess();
    }

    private void applyRoleBasedAccess() {
        User u = SessionManager.getLoggedUser();
        if (u == null) return;
        boolean isPm = "Project Manager".equals(u.roleName);
        adminSystemSection.setVisible(isPm);
        adminSystemSection.setManaged(isPm);
        adminReportsSection.setVisible(true);
        adminReportsSection.setManaged(true);
    }

    @FXML
    private void navAvailableTickets() {
        lblBreadcrumbCurrent.setText("Available Tickets");
        ViewContext.ticketMode = ViewContext.TicketViewMode.AVAILABLE;
        setView("/app/TicketBoard.fxml");
    }

    @FXML
    private void navReports() {
        lblBreadcrumbCurrent.setText("Progress Reports");
        setView("/app/ProgressReports.fxml");
    }

    @FXML
    private void navAdminPanel() {
        lblBreadcrumbCurrent.setText("Admin Panel");
        setView("/app/AdminPanel.fxml");
    }


    @FXML
    public void navProfile() {
        lblBreadcrumbCurrent.setText("My Profile");
        setView("/app/Profile.fxml");
    }

    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        try {
            SessionManager.cleanUserSession();
            AppPrefs.prefs().remove(AppPrefs.KEY_EMAIL);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene loginScene = new Scene(new FXMLLoader(getClass().getResource("/app/Login.fxml")).load(), 1200, 800);
            stage.setScene(loginScene);
            stage.setTitle("TicketFlow - Login");
            stage.centerOnScreen();
            stage.setMaximized(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private NotificationPopupController notificationPopupController;

    @FXML
    private void toggleNotifications(javafx.scene.input.MouseEvent event) {
        if (notificationPopupController == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/NotificationPopup.fxml"));
                javafx.scene.Parent root = loader.load();

                notificationPopupController = loader.getController();
             } catch (Exception e) {
                e.printStackTrace();
             }
          }

        if (notificationPopupController.isPopupShown()) {
            notificationPopupController.closePopup();
         } else {
            notificationPopupController.showNotificationsPopup(event);
         }
      }
}
