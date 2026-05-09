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
import workers.MockDataProvider;
import workers.SessionManager;
import workers.ViewContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TicketBoardController {
    @FXML private VBox colOpen, colInProgress, colPendingQA, colApproved, detailsPanel;
    @FXML private Label countOpen, countInProgress, countPendingQA, countApproved, lblActiveTicketCount, lblPageTitle;
    @FXML private Button btnCreateTicket;
     @FXML private TextField boardSearchField;

     private Node lastSelectedCard = null;
     private String boardSearchQuery = "";

     @FXML
     public void initialize() {
         initializeBoardSearch();
         User u = SessionManager.getLoggedUser();
         boolean showCreate = u != null && "Project Manager".equals(u.role)
                 && ViewContext.ticketMode == ViewContext.TicketViewMode.AVAILABLE;
         btnCreateTicket.setVisible(showCreate);
         btnCreateTicket.setManaged(showCreate);

         if (lblPageTitle != null) {
             if (ViewContext.ticketMode == ViewContext.TicketViewMode.MY_TASKS && u != null) {
                 lblPageTitle.setText("QA".equals(u.role) ? "Review Queue" : "My Tasks");
              } else {
                 lblPageTitle.setText("Board");
              }
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
         return (t.title != null && t.title.toLowerCase().contains(boardSearchQuery))
                 || (t.id != null && t.id.toLowerCase().contains(boardSearchQuery))
                 || (t.category != null && t.category.toLowerCase().contains(boardSearchQuery))
                 || (t.status != null && t.status.toLowerCase().contains(boardSearchQuery))
                 || (t.priority != null && t.priority.toLowerCase().contains(boardSearchQuery))
                 || (t.assigneeName != null && t.assigneeName.toLowerCase().contains(boardSearchQuery));
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

         for (Ticket t : tickets) {
             VBox card = createCard(t);
             switch (t.status) {
                 case "Open":
                     colOpen.getChildren().add(card);
                     break;
                 case "In Progress":
                     colInProgress.getChildren().add(card);
                     break;
                 case "Pending QA":
                     colPendingQA.getChildren().add(card);
                     break;
                 case "Approved":
                     colApproved.getChildren().add(card);
                     break;
                 default:
                     break;
              }
          }
         updateCounts();
      }

    private List<Ticket> visibleTickets() {
        List<Ticket> all = MockDataProvider.getTickets();
        if (ViewContext.ticketMode == ViewContext.TicketViewMode.AVAILABLE) {
            return all.stream().filter(t -> !"Closed".equals(t.status)).collect(Collectors.toList());
        }
        return filterMyTasks(all, SessionManager.getLoggedUser());
    }

    private List<Ticket> filterMyTasks(List<Ticket> all, User u) {
        if (u == null) return new ArrayList<>();
        List<Ticket> nonClosed = all.stream().filter(t -> !"Closed".equals(t.status)).collect(Collectors.toList());
        if ("Developer".equals(u.role)) {
            return nonClosed.stream().filter(t -> u.id.equals(t.assignedToId)).collect(Collectors.toList());
        }
        if ("QA".equals(u.role)) {
            return nonClosed.stream().filter(t ->
                    "Pending QA".equals(t.status)
                            || ("Approved".equals(t.status) && u.id.equals(t.reviewedById))
            ).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private void updateCounts() {
        countOpen.setText(String.valueOf(colOpen.getChildren().size()));
        countInProgress.setText(String.valueOf(colInProgress.getChildren().size()));
        countPendingQA.setText(String.valueOf(colPendingQA.getChildren().size()));
        countApproved.setText(String.valueOf(colApproved.getChildren().size()));
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

        Label tag = new Label(t.category);
        tag.getStyleClass().addAll("tag", "tag-" + t.category.toLowerCase());
        Label title = new Label(t.title);
        title.getStyleClass().add("ticket-title");
        HBox bottom = new HBox(8);
        bottom.setAlignment(Pos.CENTER_LEFT);
        Circle dot = new Circle(4, priorityColor(t.priority));
        bottom.getChildren().addAll(dot, new Label(t.id));
        card.getChildren().addAll(tag, title, bottom);
        return card;
    }

    private static Color priorityColor(String priority) {
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
