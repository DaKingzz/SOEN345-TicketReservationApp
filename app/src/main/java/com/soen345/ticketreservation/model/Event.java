package com.soen345.ticketreservation.model;

import java.util.Date;

/**
 * Represents an event in the Ticket Reservation Application.
 */
public class Event {
    private String eventId;
    private String name;
    private String category;
    private Date dateTime;
    private String location;
    private int totalCapacity;
    private int availableSeats;

    public Event() {}

    public Event(String eventId, String name, String category, Date dateTime,
                 String location, int totalCapacity) {
        this.eventId = eventId;
        this.name = name;
        this.category = category;
        this.dateTime = dateTime;
        this.location = location;
        this.totalCapacity = totalCapacity;
        this.availableSeats = totalCapacity;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Date getDateTime() { return dateTime; }
    public void setDateTime(Date dateTime) { this.dateTime = dateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getAvailableSeats() { return availableSeats; }
    public int getTotalCapacity() {return totalCapacity; }


}