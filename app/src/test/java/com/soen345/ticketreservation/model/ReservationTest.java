import org.junit.jupiter.api.Test;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

import com.soen345.ticketreservation.model.Reservation;

class ReservationTest {

    @Test
    void constructor_setsAllFieldsCorrectly() {
        Date reservationDate = new Date();
        Date eventDate = new Date();

        Reservation reservation = new Reservation(
                "res1",
                "event1",
                "user1",
                3,
                reservationDate,
                eventDate
        );

        assertEquals("res1", reservation.getReservationId());
        assertEquals("event1", reservation.getEventId());
        assertEquals("user1", reservation.getUserId());
        assertEquals(3, reservation.getQuantity());
        assertEquals(reservationDate, reservation.getReservationDate());
        assertEquals(eventDate, reservation.getEventDate());
    }

    @Test
    void setters_updateFieldsCorrectly() {
        Reservation reservation = new Reservation();

        Date reservationDate = new Date();
        Date eventDate = new Date();

        reservation.setReservationId("res2");
        reservation.setEventId("event2");
        reservation.setUserId("user2");
        reservation.setQuantity(5);
        reservation.setReservationDate(reservationDate);
        reservation.setEventDate(eventDate);

        assertEquals("res2", reservation.getReservationId());
        assertEquals("event2", reservation.getEventId());
        assertEquals("user2", reservation.getUserId());
        assertEquals(5, reservation.getQuantity());
        assertEquals(reservationDate, reservation.getReservationDate());
        assertEquals(eventDate, reservation.getEventDate());
    }

    @Test
    void defaultConstructor_createsEmptyObject() {
        Reservation reservation = new Reservation();

        assertNull(reservation.getReservationId());
        assertNull(reservation.getEventId());
        assertNull(reservation.getUserId());
        assertEquals(0, reservation.getQuantity());
        assertNull(reservation.getReservationDate());
        assertNull(reservation.getEventDate());
    }
}