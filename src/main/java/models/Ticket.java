package models;

import javafx.application.Application;


public class Ticket {

    private Application app;

    private Integer ticket_id;
    private String title;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private String[] categories;

    private Integer discord_thread_id;
    private String pr_url;

    private Integer assigned_user_id;

    private String closed_by;
    private String date_closed;
    private String data_added;

    public static Ticket[] getTickets(Application app){
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    public static Ticket getTicket(Application app, int id){
        throw new UnsupportedOperationException("Not yet Implemented");
    }

    public static Ticket[] getUserAssignedTickets(Application app, User user){
        throw new UnsupportedOperationException("Not yet Implemented");
    }



    // updates ticket to the server
    public void pushTicket(){}

    // fetch update
    public void pullTicket(){}




}
