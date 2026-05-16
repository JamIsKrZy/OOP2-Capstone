package workers;

import models.Ticket;
import models.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Ticket state transitions aligned with updated backend models.
 */
public final class TicketWorkflow {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

    private TicketWorkflow() {}

    private static String today() {
        return LocalDate.now().format(DATE_FMT);
    }

    public static boolean isPm(User u) {
        return u != null && u.roleName != null && "Project Manager".equalsIgnoreCase(u.roleName.trim());
    }

    public static boolean isDev(User u) {
        return u != null && u.roleName != null && "Developer".equalsIgnoreCase(u.roleName.trim());
    }

    public static boolean isQa(User u) {
        return u != null && u.roleName != null && "QA".equalsIgnoreCase(u.roleName.trim());
    }

    public static boolean canClaim(User u, Ticket t) {
        return t != null && u != null && t.getStatus() != null && "OPEN".equalsIgnoreCase(t.getStatus().trim()) && (isDev(u) || isPm(u));
    }

    public static boolean canResolve(User u) {
        return isDev(u) || isPm(u);
    }

    public static boolean canReview(User u) {
        return isQa(u) || isPm(u);
    }

    public static boolean canResolveTicket(User u, Ticket t) {
        return t != null && u != null && t.getStatus() != null && "CLAIMED".equalsIgnoreCase(t.getStatus().trim())
                && u.userId.equals(t.getClaimedBy()) && canResolve(u);
    }

    public static boolean canApprove(User u, Ticket t) {
        return t != null && u != null && t.getStatus() != null && "PENDING-REVIEW".equalsIgnoreCase(t.getStatus().trim()) && canReview(u);
    }

    public static boolean canClose(User u, Ticket t) {
        return t != null && u != null && t.getStatus() != null && ("REVIEWED".equalsIgnoreCase(t.getStatus().trim()) || "RESOLVED".equalsIgnoreCase(t.getStatus().trim()));
    }

    public static boolean canDemote(User u, Ticket t) {
        if (t == null || u == null || t.getStatus() == null) return false;
        String status = t.getStatus().trim();
        boolean assignOrPm = u.userId.equals(t.getClaimedBy()) || isPm(u);
        return ("CLAIMED".equalsIgnoreCase(status) && assignOrPm && canResolve(u))
                || ("PENDING-REVIEW".equalsIgnoreCase(status) && canReview(u));
    }

    public static void claim(Ticket t, User actor) {
        if (!canClaim(actor, t)) return;
        try {
            String jsonBody = String.format("{\"userId\": %s}", actor.userId);
            Service.APIClient.patch("/tickets/" + t.getTicketId() + "/claim", jsonBody);
            t.setStatus("CLAIMED");
            t.setClaimedBy(actor.userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resolve(Ticket t, User actor, String prUrl) {
        if (!canResolveTicket(actor, t)) return;
        try {
            String jsonBody = String.format("{\"prUrl\": \"%s\"}", prUrl);
            Service.APIClient.patch("/tickets/" + t.getTicketId() + "/resolve", jsonBody);
            t.setStatus("PENDING-REVIEW");
            t.setPrUrl(prUrl);
            actor.devScore++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void approve(Ticket t, User actor) {
        if (!canApprove(actor, t)) return;
        try {
            Service.APIClient.patch("/tickets/" + t.getTicketId() + "/review", "{}");
            t.setStatus("REVIEWED");
            actor.qaScore++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close(Ticket t, User actor) {
        if (!canClose(actor, t)) return;
        try {
            Service.APIClient.patch("/tickets/" + t.getTicketId() + "/close", "{}");
            t.setStatus("CLOSED");
            t.setClosedBy(actor.userId);
            t.setDate_closed(today());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void demote(Ticket t, User actor) {
        if (!canDemote(actor, t)) return;
        try {
            Service.APIClient.patch("/tickets/" + t.getTicketId() + "/demote", "{}");
            switch (t.getStatus()) {
                case "CLAIMED":
                    t.setStatus("OPEN");
                    t.setClaimedBy(null);
                    break;
                case "PENDING-REVIEW":
                    t.setStatus("CLAIMED");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
