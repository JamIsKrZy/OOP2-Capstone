package pages.create_ticket;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Ticket;
import models.User;
import workers.SessionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CreateTicketController {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

    @FXML private TextField txtTitle;
    @FXML private TextArea txtDesc;
    @FXML private ComboBox<String> comboCategory, comboPriority, comboStatus;

    @FXML
    public void initialize() {
        comboCategory.getItems().addAll("Bug", "Feature", "Enhancement", "Documentation");
        comboPriority.getItems().addAll("Low", "Medium", "High", "Critical");
        comboStatus.getItems().addAll("OPEN", "CLAIMED", "PENDING-REVIEW");

        comboCategory.setValue("Bug");
        comboPriority.setValue("Medium");
        comboStatus.setValue("OPEN");
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) txtTitle.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCreate() {
        User creator = SessionManager.getLoggedUser();
        if (creator == null) {
            handleCancel();
            return;
        }

        String title = txtTitle.getText() != null ? txtTitle.getText().trim() : "";
        if (title.isEmpty()) {
            return;
        }

        String status = comboStatus.getValue() != null ? comboStatus.getValue() : "OPEN";
        String desc = txtDesc.getText() != null ? txtDesc.getText().trim() : "";
        String today = LocalDate.now().format(DATE_FMT);

        // Ticket(ticketId, discordThreadId, title, description, status, prUrl, claimedBy, closedBy, priority, categories, date_added, date_closed)
        Ticket t = new Ticket(
                null, // backend generates UUID
                null, // No discord thread
                title,
                desc,
                status,
                null, // prUrl
                null, // claimedBy
                null, // closedBy
                comboPriority.getValue(),
                java.util.List.of(comboCategory.getValue()),
                today,
                null // date_closed
        );

        if ("CLAIMED".equals(status)) {
            t.setClaimedBy(creator.userId);
        }

        Ticket created = Ticket.create(t);
        if (created != null) {
            handleCancel();
        } else {
            // Show error
            System.err.println("Failed to create ticket via API");
        }
    }
}
