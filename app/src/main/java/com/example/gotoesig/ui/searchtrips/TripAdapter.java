package com.example.gotoesig.ui.searchtrips;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gotoesig.R;
import com.example.gotoesig.model.Trip;

import java.util.ArrayList;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {
    private List<Trip> trips = new ArrayList<>();
    private OnTripClickListener listener;

    public interface OnTripClickListener {
        void onTripClick(Trip trip);
    }

    public TripAdapter(OnTripClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = trips.get(position);
        holder.startPointTextView.setText("Départ : " + trip.getStartPoint());
        holder.dateTextView.setText("Date : " + trip.getDate());
        holder.timeTextView.setText("Heure : " + trip.getTime());
        holder.modeTextView.setText("Mode : " + trip.getMode());

        // Gestion du clic
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTripClick(trip);
            }
        });
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public void updateTrips(List<Trip> trips) {
        this.trips = trips;
        notifyDataSetChanged();
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView startPointTextView;
        TextView dateTextView;
        TextView timeTextView;
        TextView modeTextView;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            startPointTextView = itemView.findViewById(R.id.tv_start_point);
            dateTextView = itemView.findViewById(R.id.tv_date);
            timeTextView = itemView.findViewById(R.id.tv_time);
            modeTextView = itemView.findViewById(R.id.tv_mode);
        }
    }
}

