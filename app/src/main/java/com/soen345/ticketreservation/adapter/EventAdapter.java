package com.soen345.ticketreservation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.recyclerview.widget.RecyclerView;
import com.soen345.ticketreservation.R;
import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.OnEventInteractionListener;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private OnEventInteractionListener listener;
    
    @VisibleForTesting
    boolean isAdmin = false;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public EventAdapter(List<Event> eventList, OnEventInteractionListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
        try {
            notifyDataSetChanged();
        } catch (Exception ignored) {
            // Handle cases where notifyDataSetChanged fails in unit tests
        }
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        if (isAdmin) {
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.editBtn.setVisibility(View.VISIBLE);
            holder.bookButton.setVisibility(View.GONE);
            holder.deleteBtn.setOnClickListener(v -> listener.onDeleteClick(event, position));
            holder.editBtn.setOnClickListener(v -> listener.onEditClick(event, position));
        } else {
            holder.deleteBtn.setVisibility(View.GONE);
            holder.editBtn.setVisibility(View.GONE);
            holder.bookButton.setVisibility(View.VISIBLE);
            holder.bookButton.setOnClickListener(v -> listener.onBookClick(event, position));
        }

        holder.tvEventName.setText(event.getName());
        holder.tvEventCategory.setText(event.getCategory());
        holder.tvEventLocation.setText(event.getLocation());
        if (event.getDateTime() != null) {
            holder.tvEventDateTime.setText(dateFormat.format(event.getDateTime()));
        }
        holder.tvEventSeats.setText(String.format(Locale.getDefault(), "Seats: %d / %d", event.getAvailableSeats(), event.getTotalCapacity()));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventCategory, tvEventLocation, tvEventDateTime, tvEventSeats;
        Button deleteBtn, editBtn, bookButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            deleteBtn = itemView.findViewById(R.id.buttonDeleteEvent);
            editBtn = itemView.findViewById(R.id.buttonEditEvent);
            bookButton = itemView.findViewById(R.id.buttonBookEvent);
            bookButton = itemView.findViewById(R.id.buttonBookEvent);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvEventDateTime = itemView.findViewById(R.id.tvEventDateTime);
            tvEventSeats = itemView.findViewById(R.id.tvEventSeats);
        }
    }
}
