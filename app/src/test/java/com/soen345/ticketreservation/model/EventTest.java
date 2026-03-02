package com.soen345.ticketreservation.model;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Date;

@DisplayName("Event Model Verification")
class EventTest {

    @Test
    @DisplayName("Should correctly calculate available seats on initialization")
    void testEventCapacityLogic() {
        Date eventDate = new Date();
        Event event = new Event("id_001", "Montreal Jazz Fest", "Concert", eventDate, "Place des Arts", 500);

        assertAll("Verify all initial fields",
                () -> assertEquals(500, event.getTotalCapacity(), "Total capacity should be 500"),
                () -> assertEquals(500, event.getAvailableSeats(), "Initial available seats must match total capacity"),
                () -> assertEquals(EventCategory.CONCERT.toString(), event.getCategory(), "Category should be set correctly"),
                () -> assertEquals("id_001", event.getEventId(), "Event id set correctly"),
                () -> assertEquals("Montreal Jazz Fest", event.getName(), "Event name set correctly"),
                () -> assertEquals(eventDate, event.getDateTime(), "Event date set correctly"),
                () -> assertEquals("Place des Arts", event.getLocation(), "Event location set correctly")

        );
    }
}