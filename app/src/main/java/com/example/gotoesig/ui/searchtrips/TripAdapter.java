package com.example.gotoesig.ui.searchtrips;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gotoesig.R;
import com.example.gotoesig.model.Trip;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private List<Trip> trips;

    public TripAdapter(List<Trip> trips) {
        this.trips = trips;
    }

    public void updateTrips(List<Trip> newTrips) {
        trips = newTrips;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = trips.get(position);
        holder.tvStartPoint.setText("Départ : " + trip.getStartPoint());
        holder.tvDate.setText("Date : " + trip.getDate());
        holder.tvTime.setText("Heure : " + trip.getTime());
        holder.tvMode.setText("Mode de transport : " + trip.getMode());
    }

    @Override
    public int getItemCount() {
        return trips != null ? trips.size() : 0;
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tvStartPoint, tvDate, tvTime, tvMode;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStartPoint = itemView.findViewById(R.id.tv_start_point);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvMode = itemView.findViewById(R.id.tv_mode);
        }
    }
}
