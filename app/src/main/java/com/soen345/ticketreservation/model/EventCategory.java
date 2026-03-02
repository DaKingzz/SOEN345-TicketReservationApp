package com.soen345.ticketreservation.model;

public enum EventCategory {
    MOVIE, CONCERT, SPORT, TRAVEL;

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}