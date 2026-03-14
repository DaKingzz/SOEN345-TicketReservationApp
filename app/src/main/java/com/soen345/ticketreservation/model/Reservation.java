package com.soen345.ticketreservation.model;

import java.util.Date;

public class Reservation {
    private String reservationId;
    private String eventId;
    private String userId;
    private int quantity;
    private Date reservationDate;
    private Date eventDate;

    public Reservation() {}

    public Reservation(String reservationId, String eventId, String userId, int quantity, Date reservationDate, Date eventDate) {
        this.reservationId = reservationId;
        this.eventId = eventId;
        this.userId = userId;
        this.quantity = quantity;
        this.reservationDate = reservationDate;
        this.eventDate = eventDate;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(Date reservationDate) {
        this.reservationDate = reservationDate;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }
}
