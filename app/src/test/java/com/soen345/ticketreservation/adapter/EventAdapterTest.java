package com.soen345.ticketreservation.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import com.soen345.ticketreservation.model.OnEventInteractionListener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class EventAdapterTest {

    private List<Event> eventList;
    @Mock
    private OnEventInteractionListener listener;
    @Mock
    private EventAdapter.EventViewHolder holder;
    
    @Mock
    private Button deleteBtn;
    @Mock
    private Button editBtn;
    @Mock
    private Button bookBtn;
    @Mock
    private TextView tvEventName;
    @Mock
    private TextView tvEventCategory;
    @Mock
    private TextView tvEventLocation;
    @Mock
    private TextView tvEventDateTime;
    @Mock
    private TextView tvEventSeats;

    private EventAdapter adapter;

    @BeforeEach
    void setUp() {
        eventList = new ArrayList<>();
        Event event = new Event("1", "Concert", "Music", new Date(), "Hall", 100);
        eventList.add(event);
        
        adapter = spy(new EventAdapter(eventList, listener));

        // Setup the mock holder fields for onBindViewHolder tests
        holder.deleteBtn = deleteBtn;
        holder.editBtn = editBtn;
        holder.tvEventName = tvEventName;
        holder.tvEventCategory = tvEventCategory;
        holder.tvEventLocation = tvEventLocation;
        holder.tvEventDateTime = tvEventDateTime;
        holder.tvEventSeats = tvEventSeats;
        holder.bookButton = bookBtn;
    }

    @Test
    void getItemCount_returnsListSize() {
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    void onBindViewHolder_asAdmin_showsButtonsAndSetsListeners() {
        adapter.isAdmin = true;
        adapter.onBindViewHolder(holder, 0);

        verify(deleteBtn).setVisibility(View.VISIBLE);
        verify(editBtn).setVisibility(View.VISIBLE);
        
        ArgumentCaptor<View.OnClickListener> deleteCaptor = ArgumentCaptor.forClass(View.OnClickListener.class);
        verify(deleteBtn).setOnClickListener(deleteCaptor.capture());
        deleteCaptor.getValue().onClick(null);
        verify(listener).onDeleteClick(any(Event.class), anyInt());

        ArgumentCaptor<View.OnClickListener> editCaptor = ArgumentCaptor.forClass(View.OnClickListener.class);
        verify(editBtn).setOnClickListener(editCaptor.capture());
        editCaptor.getValue().onClick(null);
        verify(listener).onEditClick(any(Event.class), anyInt());
    }

    @Test
    void onBindViewHolder_asCustomer_hidesButtons() {
        adapter.isAdmin = false;
        adapter.onBindViewHolder(holder, 0);

        verify(deleteBtn).setVisibility(View.GONE);
        verify(editBtn).setVisibility(View.GONE);
    }

    @Test
    void onBindViewHolder_setsCorrectText() {
        Event event = eventList.get(0);
        event.setName("Jazz Night");
        event.setCategory("Jazz");
        event.setLocation("Montreal");
        event.setAvailableSeats(50);
        event.setTotalCapacity(100);
        event.setDateTime(new Date());

        adapter.onBindViewHolder(holder, 0);

        verify(tvEventName).setText("Jazz Night");
        verify(tvEventCategory).setText("Jazz");
        verify(tvEventLocation).setText("Montreal");
        verify(tvEventDateTime).setText(anyString());
        verify(tvEventSeats).setText("Seats: 50 / 100");
    }
    
    @Test
    void setAdmin_updatesIsAdmin() {
        adapter.setAdmin(true);
        assertEquals(true, adapter.isAdmin);
        adapter.setAdmin(false);
        assertEquals(false, adapter.isAdmin);
    }

    @Test
    void eventViewHolder_constructor_initializesViews() {
        View view = mock(View.class);
        when(view.findViewById(R.id.buttonDeleteEvent)).thenReturn(deleteBtn);
        when(view.findViewById(R.id.buttonEditEvent)).thenReturn(editBtn);
        when(view.findViewById(R.id.tvEventName)).thenReturn(tvEventName);
        when(view.findViewById(R.id.tvEventCategory)).thenReturn(tvEventCategory);
        when(view.findViewById(R.id.tvEventLocation)).thenReturn(tvEventLocation);
        when(view.findViewById(R.id.tvEventDateTime)).thenReturn(tvEventDateTime);
        when(view.findViewById(R.id.tvEventSeats)).thenReturn(tvEventSeats);
        when(view.findViewById(R.id.buttonBookEvent)).thenReturn(bookBtn);

        EventAdapter.EventViewHolder viewHolder = new EventAdapter.EventViewHolder(view);

        assertEquals(deleteBtn, viewHolder.deleteBtn);
        assertEquals(editBtn, viewHolder.editBtn);
        assertEquals(tvEventName, viewHolder.tvEventName);
        assertEquals(tvEventCategory, viewHolder.tvEventCategory);
        assertEquals(tvEventLocation, viewHolder.tvEventLocation);
        assertEquals(tvEventDateTime, viewHolder.tvEventDateTime);
        assertEquals(tvEventSeats, viewHolder.tvEventSeats);
        assertEquals(bookBtn, viewHolder.bookButton);
    }

    @Test
    void onCreateViewHolder_inflatesLayoutAndReturnsViewHolder() {
        ViewGroup parent = mock(ViewGroup.class);
        android.content.Context context = mock(android.content.Context.class);
        LayoutInflater inflater = mock(LayoutInflater.class);
        View view = mock(View.class);

        when(parent.getContext()).thenReturn(context);
        
        // Stub findViewById to return the correctly typed mocks to avoid ClassCastException
        when(view.findViewById(R.id.buttonDeleteEvent)).thenReturn(deleteBtn);
        when(view.findViewById(R.id.buttonEditEvent)).thenReturn(editBtn);
        when(view.findViewById(R.id.tvEventName)).thenReturn(tvEventName);
        when(view.findViewById(R.id.tvEventCategory)).thenReturn(tvEventCategory);
        when(view.findViewById(R.id.tvEventLocation)).thenReturn(tvEventLocation);
        when(view.findViewById(R.id.tvEventDateTime)).thenReturn(tvEventDateTime);
        when(view.findViewById(R.id.tvEventSeats)).thenReturn(tvEventSeats);
        when(view.findViewById(R.id.buttonBookEvent)).thenReturn(bookBtn);

        try (MockedStatic<LayoutInflater> mockedInflater = mockStatic(LayoutInflater.class)) {
            mockedInflater.when(() -> LayoutInflater.from(context)).thenReturn(inflater);
            when(inflater.inflate(eq(R.layout.item_event), eq(parent), eq(false))).thenReturn(view);

            EventAdapter.EventViewHolder result = adapter.onCreateViewHolder(parent, 0);

            assertNotNull(result);
            verify(inflater).inflate(R.layout.item_event, parent, false);
        }
    }

    private String anyString() {
        return org.mockito.ArgumentMatchers.anyString();
    }
}
