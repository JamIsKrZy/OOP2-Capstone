package models;

public class Notification {
    public String title;
    public String subtitle;
    public String description;
    public String type; // "approval", "critical"

    public Notification(String title, String subtitle, String description, String type) {
        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
        this.type = type;
     }
}