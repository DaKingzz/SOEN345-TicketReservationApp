package com.soen345.ticketreservation.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.soen345.ticketreservation.R;
import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.OnReservationInteractionListener;
import com.soen345.ticketreservation.model.Reservation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class ReservationAdapterTest {
    private List<Reservation> reservationList;
    private Map<String, Event> eventMap;
    private ReservationAdapter adapter;
    @Mock
    private OnReservationInteractionListener listener;
    @Mock
    private ReservationAdapter.ReservationViewHolder holder;
    @Mock
    private TextView tvEventName;
    @Mock
    private TextView tvReservationQuantity;
    @Mock
    private TextView tvEventCategory;
    @Mock
    private TextView tvEventLocation;
    @Mock
    private TextView tvEventDateTime;
    @Mock
    private Button cancelBtn;

    @BeforeEach
    void setUp() {
        reservationList = new ArrayList<>();
        eventMap = new HashMap<>();
        Event event = new Event("1", "Concert", "Music", new Date(), "Hall", 100);
        eventMap.put(event.getEventId(), event);
        Reservation reservation = new Reservation("1", "1", "1", 2, new Date(), event.getDateTime());
        reservationList.add(reservation);

        adapter = spy(new ReservationAdapter(reservationList, eventMap, listener));

        holder.tvEventName = tvEventName;
        holder.tvReservationQuantity = tvReservationQuantity;
        holder.tvEventCategory = tvEventCategory;
        holder.tvEventLocation = tvEventLocation;
        holder.tvEventDateTime = tvEventDateTime;
        holder.cancelBtn = cancelBtn;
    }

    @Test
    void getItemCount_returnsListSize() {
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void onBindViewHolder_setsCorrectText() {
        Reservation reservation = reservationList.get(0);
        Event event = eventMap.get(reservation.getEventId());

        adapter.onBindViewHolder(holder, 0);

        verify(tvEventName).setText(event.getName());
        verify(tvEventCategory).setText(event.getCategory());
        verify(tvEventLocation).setText(event.getLocation());
        verify(tvEventDateTime).setText(anyString());
        verify(tvReservationQuantity).setText("Quantity: " + reservation.getQuantity());
    }

    @Test
    void onCreateViewHolder_inflatesReservationLayout() {
        ViewGroup parent = mock(ViewGroup.class);
        android.content.Context context = mock(android.content.Context.class);
        LayoutInflater inflater = mock(LayoutInflater.class);
        View view = mock(View.class);

        when(parent.getContext()).thenReturn(context);

        // Stub findViewById to return the correctly typed mocks to avoid ClassCastException
        when(view.findViewById(R.id.buttonCancelEvent)).thenReturn(cancelBtn);
        when(view.findViewById(R.id.tvEventName)).thenReturn(tvEventName);
        when(view.findViewById(R.id.tvEventCategory)).thenReturn(tvEventCategory);
        when(view.findViewById(R.id.tvEventLocation)).thenReturn(tvEventLocation);
        when(view.findViewById(R.id.tvEventDateTime)).thenReturn(tvEventDateTime);
        when(view.findViewById(R.id.tvReservationQuantity)).thenReturn(tvReservationQuantity);
        try (MockedStatic<LayoutInflater> mockedInflater = mockStatic(LayoutInflater.class)) {
            mockedInflater.when(() -> LayoutInflater.from(context)).thenReturn(inflater);
            when(inflater.inflate(eq(R.layout.item_reservation), eq(parent), eq(false))).thenReturn(view);

            ReservationAdapter.ReservationViewHolder result = adapter.onCreateViewHolder(parent, 0);

            assertNotNull(result);
            verify(inflater).inflate(R.layout.item_reservation, parent, false);
        }
    }
}
