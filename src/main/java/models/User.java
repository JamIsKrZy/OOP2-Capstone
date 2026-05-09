package models;

public class User {
    public String id;
    public String name;
    public String email;
    public String role;
    /** "active" or "disabled" (React parity) */
    public String status;
    public String initials;
    public String color;
    public String discordId;
    public int inProgress;
    public int resolved;
    public int closed;
    public int reviewed;
    public int approved;

    public User(String id, String name, String email, String role, String status, String initials, String color,
                String discordId, int inProgress, int resolved, int closed, int reviewed, int approved) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
        this.initials = initials;
        this.color = color;
        this.discordId = discordId != null ? discordId : "";
        this.inProgress = inProgress;
        this.resolved = resolved;
        this.closed = closed;
        this.reviewed = reviewed;
        this.approved = approved;
    }

    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }

    public String displayStatus() {
        return "active".equalsIgnoreCase(status) ? "Active" : "Disabled";
    }
}
