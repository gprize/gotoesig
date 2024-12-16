package com.example.gotoesig.ui.statistics;

import androidx.lifecycle.ViewModelProvider;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gotoesig.R;
import com.example.gotoesig.model.Trip;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment {

    private TextView tripsCountTextView, totalAmountTextView;
    private PieChart pieChart;

    private int myTripsCount, allTripsCount;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        // Initialisation des TextViews et du PieChart
        tripsCountTextView = view.findViewById(R.id.tv_trips_count);
        totalAmountTextView = view.findViewById(R.id.tv_total_amount);
        pieChart = view.findViewById(R.id.pie_chart);

        fetchStatistics(); // Récupération des statistiques

        return view;
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
                    myTripsCount = tripCount;
                    tripsCountTextView.setText(String.valueOf(tripCount));
                    updatePieChart(myTripsCount,100);

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

//        firestore.collection("trips")
//                .get() // Récupérer tous les documents
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        if (!queryDocumentSnapshots.isEmpty()) {
//                            // Créer une liste pour stocker les trajets récupérés
//                            List<Trip> tripList = new ArrayList<>();
//                            allTripsCount = tripList.size();
//
//                            // Parcourir les documents récupérés
//                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                                // Récupérer les données du document
//                                Trip trip = documentSnapshot.toObject(Trip.class);
//
//                                // Ajouter le trajet à la liste
//                                tripList.add(trip);
//                            }
//
//                            // Utiliser la liste des trajets (par exemple, afficher dans un RecyclerView)
//                            // Par exemple, afficher le nombre de trajets
//                            Log.d("AllTrips", "Number of trips: " + tripList.size());
//                        } else {
//                            Log.d("AllTrips", "No trips found");
//                        }
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("AllTrips", "Error fetching trips", e);
//                });


    }

    private void updatePieChart(int userTrips, int otherTrips) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(userTrips, "Vos trajets"));
        entries.add(new PieEntry(otherTrips, "Tous les trajets"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.GREEN, Color.GRAY); // Couleurs des segments
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);

        pieChart.setData(pieData);
        pieChart.setCenterText("Vos trajets");
        pieChart.setCenterTextSize(16f);
        pieChart.setHoleRadius(40f); // Taille du trou central
        pieChart.setTransparentCircleRadius(45f); // Taille du cercle transparent
        pieChart.setDrawEntryLabels(false); // Masquer les labels autour du graphique
        pieChart.invalidate(); // Redessiner le graphique
    }
}
