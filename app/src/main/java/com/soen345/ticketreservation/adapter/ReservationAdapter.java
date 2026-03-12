package com.soen345.ticketreservation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.soen345.ticketreservation.R;
import com.soen345.ticketreservation.model.Event;
import com.soen345.ticketreservation.model.OnReservationInteractionListener;
import com.soen345.ticketreservation.model.Reservation;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {
    private List<Reservation> reservationList;
    private Map<String, Event> eventsMap;
    private OnReservationInteractionListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    public ReservationAdapter(List<Reservation> reservationList, Map<String, Event> eventsMap, OnReservationInteractionListener listener) {
        this.reservationList = reservationList;
        this.eventsMap = eventsMap;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservationList.get(position);
        Event event = eventsMap.get(reservation.getEventId());

        if (event == null) {
            holder.tvEventName.setText("Loading...");
            holder.tvEventCategory.setText("");
            holder.tvEventLocation.setText("");
            holder.tvEventDateTime.setText("");
            holder.tvReservationQuantity.setText(String.format(Locale.getDefault(), "Quantity: %d", reservation.getQuantity()));
            holder.cancelBtn.setEnabled(false);
            return;
        }

        holder.cancelBtn.setEnabled(true);
        holder.cancelBtn.setOnClickListener(v -> listener.onCancelClick(reservation, event, position));

        holder.tvEventName.setText(event.getName());
        holder.tvEventCategory.setText(event.getCategory());
        holder.tvEventLocation.setText(event.getLocation());
        if (event.getDateTime() != null) {
            holder.tvEventDateTime.setText(dateFormat.format(event.getDateTime()));
        } else {
            holder.tvEventDateTime.setText("");
        }
        holder.tvReservationQuantity.setText(String.format(Locale.getDefault(), "Quantity: %d", reservation.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventCategory, tvEventLocation, tvEventDateTime, tvReservationQuantity;
        Button cancelBtn;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            cancelBtn = itemView.findViewById(R.id.buttonCancelEvent);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvEventDateTime = itemView.findViewById(R.id.tvEventDateTime);
            tvReservationQuantity = itemView.findViewById(R.id.tvReservationQuantity);
        }
    }
}
