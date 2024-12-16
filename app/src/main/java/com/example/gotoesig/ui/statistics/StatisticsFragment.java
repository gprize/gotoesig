package com.example.gotoesig.ui.statistics;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gotoesig.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class StatisticsFragment extends Fragment {

    private StatisticsViewModel mViewModel;
    private TextView tripsCountTextView, totalAmountTextView;

    public static StatisticsFragment newInstance() {
        return new StatisticsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        // Initialisation des TextViews
        tripsCountTextView = view.findViewById(R.id.tv_trips_count);
        totalAmountTextView = view.findViewById(R.id.tv_total_amount);

        fetchStatistics(); // Récupération des statistiques

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
    }

    private void fetchStatistics() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        String userId = auth.getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Vous devez être connecté pour voir vos statistiques", Toast.LENGTH_SHORT).show();
            return;
        }

        // Étape 1 : Compter le nombre de trajets proposés par l'utilisateur
        firestore.collection("trips")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(tripSnapshots -> {
                    int tripCount = tripSnapshots.size();
                    tripsCountTextView.setText(String.valueOf(tripCount));

                    // Étape 2 : Calculer le montant total encaissé pour les trajets réalisés
                    tripSnapshots.getDocuments().forEach(tripDoc -> {
                        String tripId = tripDoc.getId();

                        firestore.collection("bookings")
                                .whereEqualTo("tripId", tripId)
                                .get()
                                .addOnSuccessListener(bookingSnapshots -> {
                                    double totalAmount = 0.0;

                                    for (var bookingDoc : bookingSnapshots) {
                                        Double tripAmount = bookingDoc.getDouble("amount");
                                        if (tripAmount != null) {
                                            totalAmount += tripAmount;
                                        }
                                    }

                                    // Afficher le montant total encaissé
                                    totalAmountTextView.setText(String.format("%.2f €", totalAmount));
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Erreur lors de la récupération des montants", Toast.LENGTH_SHORT).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur lors de la récupération des trajets proposés", Toast.LENGTH_SHORT).show();
                });
    }
}
