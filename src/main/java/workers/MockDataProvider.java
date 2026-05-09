package workers;

import models.Ticket;
import models.User;

import java.util.*;

/**
 * In-memory demo store (session-scoped). Mirrors the reference Ticketflow mock data shape.
 */
public final class MockDataProvider {

    private static final List<User> users = new ArrayList<>();
    private static final List<Ticket> tickets = new ArrayList<>();
    private static boolean seeded;

    private MockDataProvider() {}

    private static void ensureSeeded() {
        if (!seeded) {
            seed();
            seeded = true;
        }
    }

    public static List<User> getUsers() {
        ensureSeeded();
        return users;
    }

    public static List<Ticket> getTickets() {
        ensureSeeded();
        return tickets;
    }

    private static User lookupUser(String id) {
        if (id == null) return null;
        for (User u : users) {
            if (id.equals(u.id)) return u;
        }
        return null;
    }

    public static User findUserById(String id) {
        ensureSeeded();
        return lookupUser(id);
    }

    public static User findUserByEmail(String email) {
        ensureSeeded();
        if (email == null) return null;
        String key = email.trim().toLowerCase(Locale.ROOT);
        for (User u : users) {
            if (u.email != null && u.email.toLowerCase(Locale.ROOT).equals(key)) return u;
        }
        return null;
    }

    /** Refresh display fields from ids; safe during seed (no ensureSeeded). */
    public static void syncTicketDisplay(Ticket t) {
        User creator = lookupUser(t.createdById);
        if (creator != null) {
            t.createdBy = creator.name;
        }
        User assignee = lookupUser(t.assignedToId);
        if (assignee != null) {
            t.assigneeInitials = assignee.initials;
            t.assigneeColor = assignee.color;
            t.assigneeName = assignee.name;
            t.assigneeRole = assignee.role;
        } else {
            t.assigneeInitials = null;
            t.assigneeColor = null;
            t.assigneeName = "Unassigned";
            t.assigneeRole = "N/A";
        }
    }

    public static void addUser(User user) {
        ensureSeeded();
        users.add(user);
    }

    public static boolean deleteUserById(String id) {
        ensureSeeded();
        Iterator<User> it = users.iterator();
        while (it.hasNext()) {
            if (Objects.equals(id, it.next().id)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public static void addTicket(Ticket ticket) {
        ensureSeeded();
        syncTicketDisplay(ticket);
        tickets.add(ticket);
    }

    public static String nextTicketId() {
        ensureSeeded();
        int max = 0;
        for (Ticket t : tickets) {
            try {
                String num = t.id.replace("TKT-", "");
                max = Math.max(max, Integer.parseInt(num));
            } catch (NumberFormatException ignored) {
                // skip
            }
        }
        return "TKT-" + String.format("%03d", max + 1);
    }

    public static void rebuildDatabase() {
        users.clear();
        tickets.clear();
        seeded = false;
        ensureSeeded();
    }

    private static void seed() {
        users.clear();
        tickets.clear();

        users.add(new User("1", "Alice Johnson", "alice@example.com", "Project Manager", "active", "AJ", "#3b82f6",
                "alice#1234", 0, 45, 89, 120, 0));
        users.add(new User("2", "Bob Smith", "bob@example.com", "Developer", "active", "BS", "#4f46e5",
                "bob#5678", 2, 78, 34, 12, 0));
        users.add(new User("3", "Carol Williams", "carol@example.com", "QA", "active", "CW", "#9333ea",
                "carol#9012", 0, 5, 67, 92, 1));
        users.add(new User("4", "David Brown", "david@example.com", "Developer", "active", "DB", "#2563eb",
                "david#3456", 1, 56, 23, 8, 0));
        users.add(new User("5", "Emma Davis", "emma@example.com", "QA", "active", "ED", "#db2777",
                "emma#7890", 0, 3, 58, 74, 1));

        tickets.add(buildSeedTicket("TKT-001", "Login page not responding on mobile", "Bug", "Open", "High",
                "Users reporting white screen on iOS devices.", "April 20, 2026", "May 1, 2026",
                "1", null, null, null, null));
        tickets.add(buildSeedTicket("TKT-002", "Add dark mode toggle", "Feature", "In Progress", "Medium",
                "Implement a toggle in settings.", "April 25, 2026", "May 4, 2026",
                "1", "2", null, null, null));
        tickets.add(buildSeedTicket("TKT-003", "API response time optimization", "Enhancement", "Pending QA", "High",
                "Optimize database queries to reduce API response time", "April 28, 2026", "May 4, 2026",
                "1", "4", "4", null, null));
        tickets.add(buildSeedTicket("TKT-004", "Update user documentation", "Documentation", "Approved", "Low",
                "Finalize the user manual.", "May 3, 2026", "May 4, 2026",
                "1", "2", "2", "3", null));
        tickets.add(buildSeedTicket("TKT-005", "Memory leak in dashboard component", "Bug", "Open", "Critical",
                "High memory usage after 5 minutes.", "May 2, 2026", "May 4, 2026",
                "1", null, null, null, null));
        tickets.add(buildSeedTicket("TKT-006", "Implement export to CSV feature", "Feature", "In Progress", "Medium",
                "Add download button to reports.", "May 1, 2026", "May 4, 2026",
                "1", "4", null, null, null));
        tickets.add(buildSeedTicket("TKT-007", "Search functionality improvement", "Enhancement", "Pending QA", "Medium",
                "Improve fuzzy search results.", "May 2, 2026", "May 4, 2026",
                "1", "2", "2", null, null));
        tickets.add(buildSeedTicket("TKT-008", "Email notification system", "Feature", "Open", "Low",
                "Send alerts on status change.", "May 1, 2026", "May 4, 2026",
                "1", null, null, null, null));
        tickets.add(buildSeedTicket("TKT-009", "Fix broken image uploads", "Bug", "Closed", "High",
                "Image upload feature returns 500 error for files over 5MB.", "April 20, 2026", "April 28, 2026",
                "1", "4", "4", "5", "3"));
        tickets.add(buildSeedTicket("TKT-010", "Accessibility audit", "Enhancement", "In Progress", "Medium",
                "Run a full WCAG 2.1 check.", "May 3, 2026", "May 4, 2026",
                "1", "2", null, null, null));

        for (Ticket t : tickets) {
            syncTicketDisplay(t);
        }
    }

    private static Ticket buildSeedTicket(String id, String title, String category, String status, String priority,
                                          String description, String createdDate, String lastUpdated,
                                          String createdById, String assignedToId, String resolvedById,
                                          String reviewedById, String closedById) {
        User creator = lookupUser(createdById);
        String createdByName = creator != null ? creator.name : "";
        Ticket t = new Ticket(id, title, category, status, priority,
                null, null, "Unassigned", "N/A",
                description, createdDate, createdByName, lastUpdated, "url",
                createdById, assignedToId, resolvedById, reviewedById, closedById);
        syncTicketDisplay(t);
        return t;
    }

    public static long countUsersWithRole(String role) {
        ensureSeeded();
        long n = 0;
        for (User u : users) {
            if (role.equals(u.role) && u.isActive()) n++;
        }
        return n;
    }
}
