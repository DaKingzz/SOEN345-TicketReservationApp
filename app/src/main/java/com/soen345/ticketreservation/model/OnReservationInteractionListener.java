package com.soen345.ticketreservation.model;

public interface OnReservationInteractionListener {
    void onCancelClick(Reservation reservation, Event event, int position);
}
