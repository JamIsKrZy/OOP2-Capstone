package workers;

/** Session-wide navigation context for ticket views (same FXML, different filters). */
public final class ViewContext {

    public enum TicketViewMode {
        AVAILABLE
    }

    public static TicketViewMode ticketMode = TicketViewMode.AVAILABLE;

    private ViewContext() {}
}
