package workers;

import models.Ticket;
import models.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Ticket state transitions aligned with reference Ticketflow TicketDetailPanel / AppContext.
 */
public final class TicketWorkflow {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

    private TicketWorkflow() {}

    private static String today() {
        return LocalDate.now().format(DATE_FMT);
    }

    public static boolean isPm(User u) {
        return u != null && "Project Manager".equals(u.role);
    }

    public static boolean isDev(User u) {
        return u != null && "Developer".equals(u.role);
    }

    public static boolean isQa(User u) {
        return u != null && "QA".equals(u.role);
    }

    public static boolean canClaim(User u, Ticket t) {
        return t != null && u != null && "Open".equals(t.status) && (isDev(u) || isPm(u));
    }

    public static boolean canResolve(User u) {
        return isDev(u) || isPm(u);
    }

    public static boolean canReview(User u) {
        return isQa(u) || isPm(u);
    }

    public static boolean canResolveTicket(User u, Ticket t) {
        return t != null && u != null && "In Progress".equals(t.status)
                && u.id.equals(t.assignedToId) && canResolve(u);
    }

    public static boolean canApprove(User u, Ticket t) {
        return t != null && u != null && "Pending QA".equals(t.status) && canReview(u);
    }

    public static boolean canClose(User u, Ticket t) {
        return t != null && u != null && "Approved".equals(t.status);
    }

    public static boolean canDemote(User u, Ticket t) {
        if (t == null || u == null) return false;
        boolean assignOrPm = u.id.equals(t.assignedToId) || isPm(u);
        return ("In Progress".equals(t.status) && assignOrPm && canResolve(u))
                || ("Pending QA".equals(t.status) && canReview(u));
    }

    public static void claim(Ticket t, User actor) {
        if (!canClaim(actor, t)) return;
        t.status = "In Progress";
        t.assignedToId = actor.id;
        touch(t);
        MockDataProvider.syncTicketDisplay(t);
    }

    public static void resolve(Ticket t, User actor) {
        if (!canResolveTicket(actor, t)) return;
        t.status = "Pending QA";
        t.resolvedById = actor.id;
        actor.resolved++;
        touch(t);
        MockDataProvider.syncTicketDisplay(t);
    }

    public static void approve(Ticket t, User actor) {
        if (!canApprove(actor, t)) return;
        t.status = "Approved";
        t.reviewedById = actor.id;
        actor.reviewed++;
        touch(t);
        MockDataProvider.syncTicketDisplay(t);
    }

    public static void close(Ticket t, User actor) {
        if (!canClose(actor, t)) return;
        t.status = "Closed";
        t.closedById = actor.id;
        actor.closed++;
        touch(t);
        MockDataProvider.syncTicketDisplay(t);
    }

    public static void demote(Ticket t, User actor) {
        if (!canDemote(actor, t)) return;
        switch (t.status) {
            case "In Progress":
                t.status = "Open";
                t.assignedToId = null;
                t.resolvedById = null;
                break;
            case "Pending QA":
                t.status = "In Progress";
                t.resolvedById = null;
                break;
            default:
                return;
        }
        touch(t);
        MockDataProvider.syncTicketDisplay(t);
    }

    private static void touch(Ticket t) {
        t.lastUpdated = today();
    }
}
