package Service;

import models.Ticket;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class APIClientTest {
    @Test
    public void getTickets(){
        List<Ticket> tickets = Ticket.getTickets();
        assert  tickets != null;
        assert !tickets.isEmpty();

    }
}