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
            if (id.equals(u.userId)) return u;
        }
        return null;
    }

    public static User findUserById(String id) {
        ensureSeeded();
        return lookupUser(id);
    }

    public static User findUserByUsername(String username) {
        ensureSeeded();
        if (username == null) return null;
        String key = username.trim().toLowerCase(Locale.ROOT);
        for (User u : users) {
            if (u.username != null && u.username.toLowerCase(Locale.ROOT).equals(key)) return u;
        }
        return null;
    }

    public static User findUserByEmail(String email) {
        return findUserByUsername(email);
    }

    public static void addUser(User user) {
        ensureSeeded();
        users.add(user);
    }

    public static boolean deleteUserById(String id) {
        ensureSeeded();
        Iterator<User> it = users.iterator();
        while (it.hasNext()) {
            if (Objects.equals(id, it.next().userId)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public static void addTicket(Ticket ticket) {
        ensureSeeded();
        tickets.add(ticket);
    }

    public static String nextTicketId() {
        ensureSeeded();
        int max = 0;
        for (Ticket t : tickets) {
            try {
                String num = t.getTicketId().replace("TKT-", "");
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

        // User(userId, username, roleName, devScore, qaScore)
        users.add(new User("1", "Alice Johnson", "Project Manager", 45, 89));
        users.add(new User("2", "Bob Smith", "Developer", 78, 34));
        users.add(new User("3", "Carol Williams", "QA", 5, 67));
        users.add(new User("4", "David Brown", "Developer", 56, 23));
        users.add(new User("5", "Emma Davis", "QA", 3, 58));

        // Ticket(ticketId, discordThreadId, title, description, status, prUrl, claimedBy, closedBy, priority, categories, date_added, date_closed)
        tickets.add(new Ticket("TKT-001", "12345", "Login page not responding on mobile", "Users reporting white screen on iOS devices.", "OPEN", "https://github.com/PR-001", null, null, "High", List.of("Bug"), "April 20, 2026", null));
        tickets.add(new Ticket("TKT-002", "12346", "Add dark mode toggle", "Implement a toggle in settings.", "CLAIMED", "https://github.com/PR-002", "2", null, "Medium", List.of("Feature"), "April 25, 2026", null));
        tickets.add(new Ticket("TKT-003", "12347", "API response time optimization", "Optimize database queries to reduce API response time", "PENDING-REVIEW", "https://github.com/PR-003", "4", null, "High", List.of("Enhancement"), "April 28, 2026", null));
        tickets.add(new Ticket("TKT-004", "12348", "Update user documentation", "Finalize the user manual.", "RESOLVED", "https://github.com/PR-004", "2", null, "Low", List.of("Documentation"), "May 3, 2026", null));
        tickets.add(new Ticket("TKT-005", "12349", "Memory leak in dashboard component", "High memory usage after 5 minutes.", "OPEN", "https://github.com/PR-005", null, null, "Critical", List.of("Bug"), "May 2, 2026", null));
        tickets.add(new Ticket("TKT-006", "12350", "Implement export to CSV feature", "Add download button to reports.", "CLAIMED", "https://github.com/PR-006", "4", null, "Medium", List.of("Feature"), "May 1, 2026", null));
        tickets.add(new Ticket("TKT-007", "12351", "Search functionality improvement", "Improve fuzzy search results.", "PENDING-REVIEW", "https://github.com/PR-007", "2", null, "Medium", List.of("Enhancement"), "May 2, 2026", null));
        tickets.add(new Ticket("TKT-008", "12352", "Email notification system", "Send alerts on status change.", "OPEN", "https://github.com/PR-008", null, null, "Low", List.of("Feature"), "May 1, 2026", null));
        tickets.add(new Ticket("TKT-009", "12353", "Fix broken image uploads", "Image upload feature returns 500 error for files over 5MB.", "CLOSED", "https://github.com/PR-009", "4", "5", "High", List.of("Bug"), "April 20, 2026", "April 28, 2026"));
        tickets.add(new Ticket("TKT-010", "12354", "Accessibility audit", "Run a full WCAG 2.1 check.", "CLAIMED", "https://github.com/PR-010", "2", null, "Medium", List.of("Enhancement"), "May 3, 2026", null));
    }

    public static long countUsersWithRole(String role) {
        ensureSeeded();
        long n = 0;
        for (User u : users) {
            if (role.equals(u.roleName)) n++;
        }
        return n;
    }
}
