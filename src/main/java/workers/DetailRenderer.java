package workers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.Ticket;
import models.User;

public class DetailRenderer {

    private DetailRenderer() {}

    public static void render(VBox pane, Ticket t, Runnable onRefresh) {
        pane.getChildren().clear();
        pane.setAlignment(Pos.TOP_LEFT);
        pane.setPadding(new Insets(30));
        pane.setSpacing(25);

        User actor = SessionManager.getLoggedUser();

        Label header = new Label("Ticket Details");
        header.getStyleClass().add("detail-pane-header");
        header.setMaxWidth(Double.MAX_VALUE);

        HBox idHeader = new HBox(15);
        idHeader.setAlignment(Pos.CENTER_LEFT);
        StackPane badge = new StackPane(new Label(t.id.substring(Math.max(0, t.id.length() - 3))));
        badge.getStyleClass().add("id-circle-badge");
        ((Label) badge.getChildren().get(0)).getStyleClass().add("id-circle-text");

        VBox idTxt = new VBox(2, new Label(t.id), new Label(t.category));
        idTxt.getChildren().get(0).setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        idTxt.getChildren().get(1).setStyle("-fx-text-fill: #6b7280;");
        idHeader.getChildren().addAll(badge, idTxt);

        VBox content = new VBox(10, new Label(t.title), new Label(t.description));
        content.getChildren().get(0).setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-wrap-text: true;");
        content.getChildren().get(1).setStyle("-fx-text-fill: #4b5563; -fx-wrap-text: true; -fx-line-spacing: 5;");

        VBox attrs = new VBox(20);
        attrs.getChildren().add(row("🏷", "Status", createPill(t.status, statusStyleForStatus(t.status))));
        attrs.getChildren().add(row("⚠", "Priority", createPill(t.priority, priorityStyleForPriority(t.priority))));
        attrs.getChildren().add(row("📅", "Created", new Label(t.createdDate + "\nby " + t.createdBy)));

        User assigneeUser = MockDataProvider.findUserById(t.assignedToId);
        if (assigneeUser != null) {
            attrs.getChildren().add(row("👤", "Assigned To",
                    new Label(assigneeUser.name + "\n" + assigneeUser.role)));
        }

        Label link = new Label("View Discussion");
        link.getStyleClass().add("link-text");
        attrs.getChildren().add(row("🔗", "Discord Thread", link));

        attrs.getChildren().add(row("🕐", "Last Updated", new Label(t.lastUpdated)));

        pane.getChildren().addAll(header, idHeader, content, attrs);

        VBox actions = buildActions(t, actor, onRefresh);
        if (!actions.getChildren().isEmpty()) {
            pane.getChildren().add(actions);
        }
    }

    private static VBox buildActions(Ticket t, User actor, Runnable onRefresh) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20, 0, 0, 0));

        if (actor == null) return box;

        Button primary = primaryActionButton(t, actor, onRefresh);
        Button demote = demoteButton(t, actor, onRefresh);

        if (demote != null) {
            demote.setMaxWidth(Double.MAX_VALUE);
            demote.getStyleClass().addAll("btn");
            demote.setStyle("-fx-background-color: #fff7ed; -fx-text-fill: #c2410c; -fx-border-color: #fdba74;");
            box.getChildren().add(demote);
        }
        if (primary != null) {
            primary.setMaxWidth(Double.MAX_VALUE);
            primary.getStyleClass().add("btn");
            primary.setStyle("-fx-background-color: #059669; -fx-text-fill: white;");
            box.getChildren().add(primary);
        }
        return box;
    }

    private static Button primaryActionButton(Ticket t, User actor, Runnable onRefresh) {
        if (TicketWorkflow.canClaim(actor, t)) {
            return mkButton("Claim Ticket", () -> {
                TicketWorkflow.claim(t, actor);
                onRefresh.run();
            });
        }
        if (TicketWorkflow.canResolveTicket(actor, t)) {
            return mkButton("Mark as Resolved", () -> {
                TicketWorkflow.resolve(t, actor);
                onRefresh.run();
            });
        }
        if (TicketWorkflow.canApprove(actor, t)) {
            return mkButton("Approve Ticket", () -> {
                TicketWorkflow.approve(t, actor);
                onRefresh.run();
            });
        }
        if (TicketWorkflow.canClose(actor, t)) {
            return mkButton("Close Ticket", () -> {
                TicketWorkflow.close(t, actor);
                onRefresh.run();
            });
        }
        return null;
    }

    private static Button demoteButton(Ticket t, User actor, Runnable onRefresh) {
        if (!TicketWorkflow.canDemote(actor, t)) return null;
        String label = demoteLabel(t.status);
        return mkButton(label, () -> {
            TicketWorkflow.demote(t, actor);
            onRefresh.run();
        });
    }

    private static String demoteLabel(String status) {
        switch (status) {
            case "In Progress":
                return "Demote to Open";
            case "Pending QA":
                return "Demote to In Progress";
            default:
                return "Demote";
        }
    }

    private static Button mkButton(String text, Runnable action) {
        Button b = new Button(text);
        b.setOnAction(e -> action.run());
        return b;
    }

    private static Label createPill(String text, String style) {
        Label l = new Label(text);
        l.getStyleClass().addAll("tag", style);
        return l;
    }

    private static String statusStyleForStatus(String status) {
        switch (status) {
            case "Open":
                return "status-open";
            case "In Progress":
                return "status-progress";
            case "Pending QA":
                return "status-pending";
            case "Approved":
                return "status-approved";
            case "Closed":
                return "status-closed";
            default:
                return "status-open";
        }
    }

    private static String priorityStyleForPriority(String priority) {
        switch (priority) {
            case "Critical":
                return "tag-bug";
            case "High":
                return "tag-bug";
            case "Medium":
                return "tag-feature";
            case "Low":
                return "tag-documentation";
            default:
                return "tag-feature";
        }
    }

    private static HBox row(String icon, String label, javafx.scene.Node val) {
        HBox r = new HBox(15);
        VBox v = new VBox(5, new Label(label), val);
        ((Label) v.getChildren().get(0)).getStyleClass().add("detail-section-label");
        r.getChildren().addAll(new Label(icon), v);
        return r;
    }
}
