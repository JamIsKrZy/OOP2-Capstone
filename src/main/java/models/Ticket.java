package models;

import Service.APIClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class Ticket {
        private String ticketId;
        private String title;
        private String description;
        private String status;
        private String priority; // new update
        private List<String> categories; // new update

        private String discordThreadId;
        private String prUrl;

        private String claimedBy;

        private String closedBy;
        private String date_closed; // new update
        private String date_added; // new update

        private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();


        public Ticket(String ticketId, String discordThreadId,
                      String title, String description, String status,
                      String prUrl, String claimedBy, String closedBy,
                      String priority, List<String> categories,
                      String date_added, String date_closed) {
            this.ticketId = ticketId;
            this.title = title;
            this.description = description;
            this.priority = priority;
            this.categories = categories;

            this.discordThreadId = discordThreadId;
            this.prUrl = prUrl;


            this.status = status;

            this.claimedBy = claimedBy;

            this.closedBy = closedBy;
            this.date_closed = date_closed;
            this.date_added = date_added;

            validateStatus();
        }

        private void validateStatus(){
            switch (this.status){
                case "OPEN" -> {}
                case "CLAIMED" -> {}
                case "PENDING-REVIEW" -> {}
                case "REVIEWED" -> {}
                case "RESOLVED" -> {}
                case "CLOSED" -> {}
                default -> {
                    status = "OPEN";
                }
            }
        }

        // Getters

        public String getTicketId() { return ticketId; }
        public String getDiscordThreadId() { return discordThreadId; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
        public String getPrUrl() { return prUrl; }
        public String getClaimedBy() { return claimedBy; }
        public String getClosedBy() { return closedBy; }
        public String getPriority() {return priority;}
        public List<String> getCategories() {return categories;}
        public String getDate_closed() {return date_closed;}
        public String getDate_added() {return date_added;}

        // Setters
        public void setDiscordThreadId(String discordThreadId) { this.discordThreadId = discordThreadId; }
        public void setTitle(String title) { this.title = title; }
        public void setDescription(String description) { this.description = description; }
        public void setStatus(String status) { this.status = status; }
        public void setPrUrl(String prUrl) { this.prUrl = prUrl; }
        public void setClaimedBy(String claimedBy) { this.claimedBy = claimedBy; }
        public void setClosedBy(String closedBy) { this.closedBy = closedBy; }
        public void setPriority(String priority) { this.priority = priority; }
        public void setCategories(List<String> categories) { this.categories = categories; }
        public void setDate_closed(String date_closed) { this.date_closed = date_closed; }
        public void setDate_added(String date_added) { this.date_added = date_added; }
        public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public static List<Ticket> getTickets(){
        String request_body;
        List<Ticket> tickets = null;
        try {
            request_body = APIClient.get("tickets/list");
            tickets = OBJECT_MAPPER.readValue(request_body, new TypeReference<List<Ticket>>() {});
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

        return tickets ;
    }

    public static Ticket getTicket(int id){
        throw new UnsupportedOperationException("Not yet Implemented!");
    }

    public void push(){
        throw new UnsupportedOperationException("Not yet Implemented!");
    }

    public void fetch(){
        throw new UnsupportedOperationException("Not yet Implemented!");
    }


}
