package com.soen345.ticketreservation.model;

public interface OnEventInteractionListener {
    void onDeleteClick(Event event, int position);
    void onEditClick(Event event, int position);
    void onBookClick(Event event, int position);
}
