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
        return u != null && "Project Manager".equals(u.roleName);
    }

    public static boolean isDev(User u) {
        return u != null && "Developer".equals(u.roleName);
    }

    public static boolean isQa(User u) {
        return u != null && "QA".equals(u.roleName);
    }

    public static boolean canClaim(User u, Ticket t) {
        return t != null && u != null && "OPEN".equals(t.getStatus()) && (isDev(u) || isPm(u));
    }

    public static boolean canResolve(User u) {
        return isDev(u) || isPm(u);
    }

    public static boolean canReview(User u) {
        return isQa(u) || isPm(u);
    }

    public static boolean canResolveTicket(User u, Ticket t) {
        return t != null && u != null && "CLAIMED".equals(t.getStatus())
                && u.userId.equals(t.getClaimedBy()) && canResolve(u);
    }

    public static boolean canApprove(User u, Ticket t) {
        return t != null && u != null && "PENDING-REVIEW".equals(t.getStatus()) && canReview(u);
    }

    public static boolean canClose(User u, Ticket t) {
        return t != null && u != null && ("REVIEWED".equals(t.getStatus()) || "RESOLVED".equals(t.getStatus()));
    }

    public static boolean canDemote(User u, Ticket t) {
        if (t == null || u == null) return false;
        boolean assignOrPm = u.userId.equals(t.getClaimedBy()) || isPm(u);
        return ("CLAIMED".equals(t.getStatus()) && assignOrPm && canResolve(u))
                || ("PENDING-REVIEW".equals(t.getStatus()) && canReview(u));
    }

    public static void claim(Ticket t, User actor) {
        if (!canClaim(actor, t)) return;
        t.setStatus("CLAIMED");
        t.setClaimedBy(actor.userId);
    }

    public static void resolve(Ticket t, User actor) {
        if (!canResolveTicket(actor, t)) return;
        t.setStatus("PENDING-REVIEW");
        actor.devScore++;
    }

    public static void approve(Ticket t, User actor) {
        if (!canApprove(actor, t)) return;
        t.setStatus("REVIEWED");
        actor.qaScore++;
    }

    public static void close(Ticket t, User actor) {
        if (!canClose(actor, t)) return;
        t.setStatus("CLOSED");
        t.setClosedBy(actor.userId);
        t.setDate_closed(today());
    }

    public static void demote(Ticket t, User actor) {
        if (!canDemote(actor, t)) return;
        switch (t.getStatus()) {
            case "CLAIMED":
                t.setStatus("OPEN");
                t.setClaimedBy(null);
                break;
            case "PENDING-REVIEW":
                t.setStatus("CLAIMED");
                break;
            default:
                return;
        }
    }
}
