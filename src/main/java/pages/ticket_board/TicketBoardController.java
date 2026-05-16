package pages.ticket_board;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Ticket;
import models.User;
import pages.dashboard.MainController;
import workers.DetailRenderer;
import workers.SessionManager;
import workers.ViewContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TicketBoardController {
    @FXML private VBox colOpen, colInProgress, colPendingQA, colApproved, colClosed, detailsPanel;
    @FXML private Label countOpen, countInProgress, countPendingQA, countApproved, countClosed, lblActiveTicketCount, lblPageTitle;
    @FXML private Button btnCreateTicket;
     @FXML private TextField boardSearchField;

     private Node lastSelectedCard = null;
     private String boardSearchQuery = "";

     @FXML
     public void initialize() {
         initializeBoardSearch();
         User u = SessionManager.getLoggedUser();
         boolean showCreate = u != null && "Project Manager".equals(u.roleName)
                 && ViewContext.ticketMode == ViewContext.TicketViewMode.AVAILABLE;
         btnCreateTicket.setVisible(showCreate);
         btnCreateTicket.setManaged(showCreate);

         if (lblPageTitle != null) {
             lblPageTitle.setText("Board");
         }

         refreshBoard();
      }

     private void initializeBoardSearch() {
         if (boardSearchField != null) {
             boardSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                 boardSearchQuery = newVal != null ? newVal.toLowerCase() : "";
                 refreshBoard();
               });
           }
       }

     private boolean ticketMatchesBoardSearch(Ticket t) {
         if (boardSearchQuery.isEmpty()) return true;
         String cats = t.getCategories() != null ? String.join(", ", t.getCategories()) : "";
         User u = User.findUserById(t.getClaimedBy());
         String assigneeName = u != null ? u.username : "Unassigned";
         
         return (t.getTitle() != null && t.getTitle().toLowerCase().contains(boardSearchQuery))
                 || (t.getTicketId() != null && t.getTicketId().toLowerCase().contains(boardSearchQuery))
                 || (cats.toLowerCase().contains(boardSearchQuery))
                 || (t.getStatus() != null && t.getStatus().toLowerCase().contains(boardSearchQuery))
                 || (t.getPriority() != null && t.getPriority().toLowerCase().contains(boardSearchQuery))
                 || (assigneeName.toLowerCase().contains(boardSearchQuery));
       }

     public void refreshBoard() {
         List<Ticket> tickets = visibleTickets();
          // Filter by search query
         if (!boardSearchQuery.isEmpty()) {
             tickets = tickets.stream().filter(this::ticketMatchesBoardSearch).collect(Collectors.toList());
           }
         lblActiveTicketCount.setText(tickets.size() + " active tickets");
         colOpen.getChildren().clear();
         colInProgress.getChildren().clear();
         colPendingQA.getChildren().clear();
         colApproved.getChildren().clear();
         colClosed.getChildren().clear();

         for (Ticket t : tickets) {
             VBox card = createCard(t);
             String status = t.getStatus();
             if (status == null) continue;
             
             switch (status) {
                 case "OPEN":
                     colOpen.getChildren().add(card);
                     break;
                 case "CLAIMED":
                     colInProgress.getChildren().add(card);
                     break;
                 case "PENDING-REVIEW":
                 case "IN_REVIEW":
                     colPendingQA.getChildren().add(card);
                     break;
                 case "REVIEWED":
                 case "RESOLVED":
                     colApproved.getChildren().add(card);
                     break;
                 case "CLOSED":
                     colClosed.getChildren().add(card);
                     break;
                 default:
                     break;
              }
          }
         updateCounts();
       }

    private List<Ticket> visibleTickets() {
        List<Ticket> all = Ticket.getTickets();
        return all != null ? all : new ArrayList<>();
    }

    private void updateCounts() {
        countOpen.setText(String.valueOf(colOpen.getChildren().size()));
        countInProgress.setText(String.valueOf(colInProgress.getChildren().size()));
        countPendingQA.setText(String.valueOf(colPendingQA.getChildren().size()));
        countApproved.setText(String.valueOf(colApproved.getChildren().size()));
        countClosed.setText(String.valueOf(colClosed.getChildren().size()));
    }

    private VBox createCard(Ticket t) {
        VBox card = new VBox(12);
        card.getStyleClass().add("ticket-card");

        card.setOnMouseClicked(e -> {
            if (lastSelectedCard != null) {
                lastSelectedCard.getStyleClass().remove("selected-ticket");
            }
            card.getStyleClass().add("selected-ticket");
            lastSelectedCard = card;
            DetailRenderer.render(detailsPanel, t, this::refreshBoard);
        });

        String catName = (t.getCategories() != null && !t.getCategories().isEmpty()) ? t.getCategories().get(0) : "N/A";
        Label tag = new Label(catName);
        tag.getStyleClass().addAll("tag", "tag-feature");
        Label title = new Label(t.getTitle());
        title.getStyleClass().add("ticket-title");
        HBox bottom = new HBox(8);
        bottom.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4, priorityColor(t.getPriority()));
        bottom.getChildren().addAll(dot, new Label(t.getTicketId()));
        card.getChildren().addAll(tag, title, bottom);
        return card;
    }

    private static Color priorityColor(String priority) {
        if (priority == null) return Color.valueOf("#22c55e");
        switch (priority) {
            case "Critical":
                return Color.valueOf("#dc2626");
            case "High":
                return Color.valueOf("#ea580c");
            case "Medium":
                return Color.valueOf("#f59e0b");
            case "Low":
            default:
                return Color.valueOf("#22c55e");
        }
    }

    @FXML
    private void openCreateTicketModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/CreateTicketModal.fxml"));
            VBox root = loader.load();
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();
            refreshBoard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToList() {
        MainController.getInstance().setView("/app/TicketList.fxml");
    }
}
