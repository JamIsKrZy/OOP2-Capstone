package pages.ticket_list;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import models.Ticket;
import models.User;
import pages.dashboard.MainController;
import workers.DetailRenderer;
import workers.SessionManager;
import workers.ViewContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

public class TicketListController {

    @FXML private VBox listContainer, detailsPanel;
    @FXML private Label lblActiveTicketCount;
    @FXML private Button btnCreateTicket;

    // Filter elements
    @FXML private TextField filterTicketField;
    @FXML private ComboBox<String> filterCategoryCombo;
    @FXML private ComboBox<String> filterStatusCombo;
    @FXML private ComboBox<String> filterPriorityCombo;
    @FXML private ComboBox<String> filterAssigneeCombo;

    private Node lastSelectedRow = null;

    // Filter values
    private String selectedCategory = "All";
    private String selectedStatus = "All";
    private String selectedPriority = "All";
    private String selectedAssignee = "All";
    private String filterTicketQuery = "";

    // Observable lists for ComboBoxes
    private final ObservableList<String> categoryOptions = FXCollections.observableArrayList();
    private final ObservableList<String> statusOptions = FXCollections.observableArrayList(
        "All", "OPEN", "CLAIMED", "PENDING-REVIEW", "REVIEWED", "RESOLVED", "CLOSED"
    );
    private final ObservableList<String> priorityOptions = FXCollections.observableArrayList(
        "All", "Low", "Medium", "High", "Critical"
    );
    private final ObservableList<String> assigneeOptions = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        User u = SessionManager.getLoggedUser();
        boolean showCreate = u != null && "Project Manager".equals(u.roleName)
                 && ViewContext.ticketMode == ViewContext.TicketViewMode.AVAILABLE;
        if (btnCreateTicket != null) {
            btnCreateTicket.setVisible(showCreate);
            btnCreateTicket.setManaged(showCreate);
        }

        // Initialize filter dropdowns
        initializeFilterDropdowns();

        // Initialize filter text field listener
        initializeFilterTextField();

        refreshList();
    }

    private void initializeFilterDropdowns() {
        // Set up category combo box
        filterCategoryCombo.setItems(categoryOptions);
        filterCategoryCombo.setValue("All");

        // Set up status combo box
        filterStatusCombo.setItems(statusOptions);
        filterStatusCombo.setValue("All");

        // Set up priority combo box
        filterPriorityCombo.setItems(priorityOptions);
        filterPriorityCombo.setValue("All");

        // Set up assignee combo box
        filterAssigneeCombo.setItems(assigneeOptions);
        filterAssigneeCombo.setValue("All");

        // Add change listeners
        filterCategoryCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedCategory = newVal != null ? newVal : "All";
            refreshList();
        });

        filterStatusCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedStatus = newVal != null ? newVal : "All";
            refreshList();
        });

        filterPriorityCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedPriority = newVal != null ? newVal : "All";
            refreshList();
        });

        filterAssigneeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedAssignee = newVal != null ? newVal : "All";
            refreshList();
        });

        // Populate dynamic options from current tickets
        populateDynamicOptions();
    }

    private void populateDynamicOptions() {
        List<Ticket> allTickets = Ticket.getTickets();
        if (allTickets == null) return;

        // Get unique categories
        Set<String> categories = new HashSet<>();
        for (Ticket t : allTickets) {
            if (t.getCategories() != null) {
                categories.addAll(t.getCategories());
            }
        }
        categoryOptions.clear();
        categoryOptions.add("All");
        categoryOptions.addAll(categories.stream().filter(c -> c != null && !c.isEmpty()).sorted().collect(Collectors.toList()));

        // Get unique assignees
        Set<String> assignees = new HashSet<>();
        assignees.add("Unassigned");
        for (Ticket t : allTickets) {
            User u = User.findUserById(t.getClaimedBy());
            if (u != null) {
                assignees.add(u.username);
            }
        }
        assigneeOptions.clear();
        assigneeOptions.add("All");
        assigneeOptions.addAll(assignees.stream().sorted().collect(Collectors.toList()));
    }

    private void initializeFilterTextField() {
        if (filterTicketField != null) {
            filterTicketField.textProperty().addListener((obs, oldVal, newVal) -> {
                filterTicketQuery = newVal != null ? newVal.toLowerCase() : "";
                refreshList();
            });
        }
    }

    private boolean ticketMatchesFilters(Ticket t) {
        // Filter by ticket text field (searches ID and title)
        if (!filterTicketQuery.isEmpty()) {
            boolean matchesId = t.getTicketId() != null && t.getTicketId().toLowerCase().contains(filterTicketQuery);
            boolean matchesTitle = t.getTitle() != null && t.getTitle().toLowerCase().contains(filterTicketQuery);
            if (!matchesId && !matchesTitle) return false;
        }

        // Filter by category
        if (!"All".equals(selectedCategory)) {
            if (t.getCategories() == null || !t.getCategories().contains(selectedCategory)) {
                return false;
            }
        }

        // Filter by status
        if (!"All".equals(selectedStatus) && (t.getStatus() == null || !t.getStatus().equals(selectedStatus))) {
            return false;
        }

        // Filter by priority
        if (!"All".equals(selectedPriority) && (t.getPriority() == null || !t.getPriority().equals(selectedPriority))) {
            return false;
        }

        // Filter by assignee
        if (!"All".equals(selectedAssignee)) {
            User u = User.findUserById(t.getClaimedBy());
            String ticketAssignee = u != null ? u.username : "Unassigned";
            if (!ticketAssignee.equals(selectedAssignee)) {
                return false;
            }
        }

        return true;
    }

    public void refreshList() {
        listContainer.getChildren().clear();
        List<Ticket> tickets = visibleTickets();

        // Apply filter
        tickets = tickets.stream().filter(this::ticketMatchesFilters).collect(Collectors.toList());

        lblActiveTicketCount.setText(tickets.size() + " active tickets");

        for (Ticket t : tickets) {
            HBox row = createListRow(t);
            listContainer.getChildren().add(row);
        }
    }

    private List<Ticket> visibleTickets() {
        List<Ticket> all = Ticket.getTickets();
        if (all == null) all = new ArrayList<>();
        if (ViewContext.ticketMode == ViewContext.TicketViewMode.AVAILABLE) {
            return all.stream().filter(t -> !"CLOSED".equals(t.getStatus())).collect(Collectors.toList());
        }
        return filterMyTasks(all, SessionManager.getLoggedUser());
    }

    private List<Ticket> filterMyTasks(List<Ticket> all, User u) {
        if (u == null) return new ArrayList<>();
        List<Ticket> nonClosed = all.stream().filter(t -> !"CLOSED".equals(t.getStatus())).collect(Collectors.toList());
        if ("Developer".equals(u.roleName)) {
            return nonClosed.stream().filter(t -> u.userId.equals(t.getClaimedBy())).collect(Collectors.toList());
        }
        if ("QA".equals(u.roleName)) {
            return nonClosed.stream().filter(t ->
                     "PENDING-REVIEW".equals(t.getStatus())
                            || (("REVIEWED".equals(t.getStatus()) || "RESOLVED".equals(t.getStatus())) && u.userId.equals(t.getClosedBy()))
            ).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private HBox createListRow(Ticket t) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("list-row");

        row.setOnMouseClicked(e -> {
            if (lastSelectedRow != null) {
                lastSelectedRow.getStyleClass().remove("selected-ticket");
            }
            row.getStyleClass().add("selected-ticket");
            lastSelectedRow = row;

            DetailRenderer.render(detailsPanel, t, this::refreshList);
        });

        String tid = t.getTicketId();
        VBox tBox = createCol(new VBox(2, boldLabel(t.getTitle()), mutedLabel(tid)), 300);
        String catLabel = (t.getCategories() != null && !t.getCategories().isEmpty()) ? t.getCategories().get(0) : "N/A";
        StackPane cat = createCol(new StackPane(tag(catLabel, "tag-feature")), 120);
        StackPane stat = createCol(new StackPane(tag(t.getStatus(), getStatusStyle(t.getStatus()))), 120);
        StackPane prio = createCol(new StackPane(new Label(t.getPriority())), 100);
        prio.getChildren().get(0).getStyleClass().add("priority-" + t.getPriority().toLowerCase());

        User u = User.findUserById(t.getClaimedBy());
        String assigneeName = u != null ? u.username : "Unassigned";
        Label assignee = new Label(assigneeName);
        assignee.setStyle("-fx-text-fill: #111827;");
        StackPane assCol = createCol(new StackPane(assignee), 180);

        row.getChildren().addAll(tBox, cat, stat, prio, assCol);
        return row;
    }

    private <T extends Pane> T createCol(T pane, double width) {
        pane.setMinWidth(width);
        pane.setPrefWidth(width);
        pane.setMaxWidth(width);
        if (pane instanceof StackPane) {
            ((StackPane) pane).setAlignment(Pos.CENTER_LEFT);
        }
        return pane;
    }

    private Label boldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827;");
        return l;
    }

    private Label mutedLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 11px;");
        return l;
    }

    private Label tag(String text, String style) {
        Label l = new Label(text);
        l.getStyleClass().addAll("tag", style);
        return l;
    }

    private String getStatusStyle(String s) {
        if (s == null) return "status-open";
        switch (s) {
            case "OPEN":
                return "status-open";
            case "CLAIMED":
                return "status-progress";
            case "PENDING-REVIEW":
                return "status-pending";
            case "REVIEWED":
            case "RESOLVED":
                return "status-approved";
            case "CLOSED":
                return "status-closed";
            default:
                return "status-open";
        }
    }

    @FXML
    private void switchToBoard() {
        MainController.getInstance().setView("/app/TicketBoard.fxml");
    }

    @FXML
    private void openCreateTicketModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/CreateTicketModal.fxml"));
            VBox root = loader.load();
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();
            // Refresh filter options after creating a ticket
            populateDynamicOptions();
            refreshList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}