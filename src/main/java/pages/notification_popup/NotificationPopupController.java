package pages.notification_popup;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.scene.input.MouseEvent;
import models.Notification;
import models.Ticket;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the NotificationPopup FXML.
 * Handles rendering notification items and managing popup lifecycle.
 */
public class NotificationPopupController {

    @FXML private VBox popupContent;
    @FXML private VBox notificationList;
    @FXML private Label headerTitle;
    @FXML private javafx.scene.control.Button btnClose;

	private Popup notificationPopup;
    private boolean notificationsPopupShown = false;

     /**
      * Initialize the controller and bind UI events.
      */
     @FXML
    public void initialize() {
         // Setup close button
        if (btnClose != null) {
            btnClose.setOnMouseClicked(e -> closePopup());
         }
     }

     /**
      * Handles the close button click event.
      */
     @FXML
    private void handleClose() {
        closePopup();
     }

     /**
      * Opens/shows the notification popup with populated notification items.
      * Uses JavaFX Popup which auto-handles click-outside-to-close.
      *
      * @param event the mouse event (used for positioning), may be null
      */
    public void showNotificationsPopup(MouseEvent event) {
        try {
             // Clear any existing notification items
            notificationList.getChildren().clear();

             // Create mock notifications based on active tickets
            List<Notification> notifications = new ArrayList<>();
            List<Ticket> tickets = Ticket.getTickets();

             // Add some sample notifications
            notifications.add(new Notification("New Bug Report",
                     "TKT-005: Memory leak in dashboard component", "TKT-005: Memory leak in dashboard component was assigned to you.", "critical"));
            notifications.add(new Notification("Approval Request",
                     "TKT-003: API response time optimization", "TKT-003: API response time optimization is pending your review.", "approval"));
            notifications.add(new Notification("Status Changed",
                     "TKT-002: Dark mode toggle", "TKT-002: Dark mode toggle moved to In Progress.", "approval"));

             // Populate notification items
            for (Notification notif : notifications) {
                VBox notifCard = createNotificationCard(notif);
                notificationList.getChildren().add(notifCard);
             }

             // Show the popup
            showPopup(event);

         } catch (Exception e) {
            e.printStackTrace();
         }
     }

     /**
      * Creates a notification card VBox for a given notification.
      * Uses a 3-tier text layout: Title (Bold, Dark), Subtitle (Medium Gray), Details (Light Gray).
      * Matches the modal window styling with border and shadow.
      */
    private VBox createNotificationCard(Notification notif) {
        VBox notifCard = new VBox(8);
        notifCard.getStyleClass().add("notif-item-row");
        notifCard.setMinWidth(500);

        // Determine emoji based on type
        String emoji;
        if ("critical".equals(notif.type)) {
            emoji = "⚠️";
         } else {
            emoji = "✅";
         }

        // Tier 1: Title (Bold, Dark)
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 16px;");

        Label titleLabel = new Label(notif.title);
        titleLabel.getStyleClass().add("notif-title");

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, javafx.scene.layout.Priority.ALWAYS);

        Label timeAgo = new Label("just now");
        timeAgo.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");

        titleRow.getChildren().addAll(emojiLabel, titleLabel, titleSpacer, timeAgo);

        // Tier 2: Subtitle (Medium Gray)
        Label subtitleLabel = new Label(notif.subtitle != null ? notif.subtitle : "");
        subtitleLabel.getStyleClass().add("notif-subtitle");

        // Tier 3: Details (Light Gray)
        Label detailsLabel = new Label(notif.description);
        detailsLabel.getStyleClass().add("notif-details");

        notifCard.getChildren().addAll(titleRow, subtitleLabel, detailsLabel);

        return notifCard;
     }

      /**
       * Creates and shows the popup using JavaFX Popup.
       * Popup automatically handles click-outside-to-close behavior.
       */
	private void showPopup(MouseEvent event) {
			// Create the popup
		notificationPopup = new Popup();
		notificationPopup.setAutoHide(true); // Auto-hide when mouse leaves or clicks outside

			// Set the content
		notificationPopup.getContent().setAll(popupContent);

			// Set size on the content node (Popup doesn't have setWidth/setHeight)
		popupContent.setPrefSize(550, 500);

			// Position: X = button_screen_x - popup_width + button_width
			// Y = button_screen_y + button_height + 10 (10px margin below)
		if (event != null) {
			double buttonScreenX = event.getScreenX();
			double buttonScreenY = event.getScreenY();
			javafx.scene.Parent buttonNode = (javafx.scene.Parent) event.getSource();
			double buttonWidth = buttonNode.getBoundsInParent().getWidth();
			double buttonHeight = buttonNode.getBoundsInParent().getHeight();

			 double popupWidth = 550;

			double x = buttonScreenX - popupWidth + buttonWidth;
			double y = buttonScreenY + buttonHeight + 10;

			notificationPopup.show(buttonNode.getScene().getWindow(), x, y);
		} else {
			// Fallback: try to find window from existing FXML components
			if (popupContent.getScene() != null) {
				notificationPopup.show(popupContent.getScene().getWindow(), 900, 30);
			} else if (btnClose.getScene() != null) {
				notificationPopup.show(btnClose.getScene().getWindow(), 900, 30);
			} else {
				// Final fallback: explicit cast to resolve ambiguity
				notificationPopup.show((javafx.stage.Window) null, 900, 30);
			}
		}

        notificationsPopupShown = true;
      }

     /**
      * Closes the popup programmatically.
      */
    public void closePopup() {
        if (notificationPopup != null) {
            notificationPopup.hide();
            notificationsPopupShown = false;
         }
     }

     /**
      * Returns whether the popup is currently shown.
      */
    public boolean isPopupShown() {
        return notificationsPopupShown;
     }
}