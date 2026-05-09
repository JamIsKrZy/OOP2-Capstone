package models;

public class Ticket {
    public String id;
    public String title;
    public String category;
    public String status;
    public String priority;
    public String assigneeInitials;
    public String assigneeColor;
    public String assigneeName;
    public String assigneeRole;
    public String description;
    public String createdDate;
    /** Display name of creator */
    public String createdBy;
    public String lastUpdated;
    public String discordUrl;

    public String createdById;
    public String assignedToId;
    public String resolvedById;
    public String reviewedById;
    public String closedById;

    public Ticket(String id, String title, String category, String status, String priority,
                  String assigneeInitials, String assigneeColor, String assigneeName, String assigneeRole,
                  String description, String createdDate, String createdBy, String lastUpdated, String discordUrl,
                  String createdById, String assignedToId, String resolvedById, String reviewedById, String closedById) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.status = status;
        this.priority = priority;
        this.assigneeInitials = assigneeInitials;
        this.assigneeColor = assigneeColor;
        this.assigneeName = assigneeName;
        this.assigneeRole = assigneeRole;
        this.description = description;
        this.createdDate = createdDate;
        this.createdBy = createdBy;
        this.lastUpdated = lastUpdated;
        this.discordUrl = discordUrl;
        this.createdById = createdById;
        this.assignedToId = assignedToId;
        this.resolvedById = resolvedById;
        this.reviewedById = reviewedById;
        this.closedById = closedById;
    }
}
