package com.soen345.ticketreservation.util;

import com.soen345.ticketreservation.model.Event;

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class EventFilter {

    /**
     * Filters a list of events based on category, location, and date.
     *
     * @param events           The original list of events.
     * @param category         The category to filter by (empty string for all).
     * @param locationQuery    The location query (empty string for all).
     * @param selectedCalendar The specific date to filter by (null for all).
     * @return A filtered list of events.
     */
    public static List<Event> filter(List<Event> events, String category, String locationQuery, Calendar selectedCalendar) {
        String finalLocationQuery = locationQuery != null ? locationQuery.toLowerCase().trim() : "";
        
        return events.stream()
                .filter(e -> {
                    // Category Filter
                    boolean categoryMatch = category == null || category.isEmpty() ||
                            (e.getCategory() != null && e.getCategory().equalsIgnoreCase(category));

                    // Location Filter
                    boolean locationMatch = finalLocationQuery.isEmpty() ||
                            (e.getLocation() != null && e.getLocation().toLowerCase().contains(finalLocationQuery));

                    // Date Filter
                    boolean dateMatch = true;
                    if (selectedCalendar != null && e.getDateTime() != null) {
                        Calendar eventCal = Calendar.getInstance();
                        eventCal.setTime(e.getDateTime());
                        dateMatch = eventCal.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR) &&
                                eventCal.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR);
                    }

                    return categoryMatch && locationMatch && dateMatch;
                })
                .collect(Collectors.toList());
    }
}
