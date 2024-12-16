package com.example.gotoesig.ui.searchtrips;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gotoesig.R;
import com.example.gotoesig.model.Booking;
import com.example.gotoesig.model.Trip;

import java.util.ArrayList;
import java.util.List;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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

        holder.btnRegister.setOnClickListener(v -> {
            // Inscrire l'utilisateur sur ce trajet
            registerUserForTrip(trip,holder);
        });

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
        Button btnRegister;
        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            startPointTextView = itemView.findViewById(R.id.tv_start_point);
            dateTextView = itemView.findViewById(R.id.tv_date);
            timeTextView = itemView.findViewById(R.id.tv_time);
            modeTextView = itemView.findViewById(R.id.tv_mode);
            btnRegister = itemView.findViewById(R.id.btn_register);
        }
    }

    // Méthode pour inscrire l'utilisateur au trajet
    private void registerUserForTrip(Trip trip, TripViewHolder holder) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(holder.itemView.getContext(), "Vous devez être connecté pour vous inscrire", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // Référence à la collection 'trips' pour obtenir l'ID du document correspondant au trip
        firestore.collection("trips")
                .whereEqualTo("startPoint", trip.getStartPoint()) // Filtrer selon les données disponibles dans Trip
                .whereEqualTo("endPoint", trip.getEndPoint())
                .whereEqualTo("date", trip.getDate())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(holder.itemView.getContext(), "Trajet introuvable", Toast.LENGTH_SHORT).show();
                    } else {
                        // Récupérer l'ID du document trip
                        String tripId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Rechercher ou créer une réservation
                        firestore.collection("bookings")
                                .whereEqualTo("tripId", tripId)
                                .get()
                                .addOnSuccessListener(bookingSnapshots -> {
                                    if (bookingSnapshots.isEmpty()) {
                                        // Si aucune réservation n'existe pour ce trajet, créer un nouveau document
                                        Booking newBooking = new Booking(tripId);
                                        newBooking.addUser(userId);

                                        firestore.collection("bookings")
                                                .add(newBooking)
                                                .addOnSuccessListener(documentReference -> {
                                                    Toast.makeText(holder.itemView.getContext(), "Réservation créée avec succès", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(holder.itemView.getContext(), "Erreur lors de la création de la réservation", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        // Si une réservation existe, mettre à jour la liste des participants
                                        String bookingId = bookingSnapshots.getDocuments().get(0).getId();
                                        firestore.collection("bookings")
                                                .document(bookingId)
                                                .update("userIds", FieldValue.arrayUnion(userId))
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(holder.itemView.getContext(), "Ajouté au trajet avec succès", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(holder.itemView.getContext(), "Erreur lors de l'ajout au trajet", Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(holder.itemView.getContext(), "Erreur lors de la vérification des réservations existantes", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "Erreur lors de la vérification du trajet", Toast.LENGTH_SHORT).show();
                });
    }

}

