package com.example.gotoesig.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gotoesig.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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

        tripsCountTextView = view.findViewById(R.id.tv_trips_count);
        totalAmountTextView = view.findViewById(R.id.tv_total_amount);
        pieChart = view.findViewById(R.id.pie_chart);

        fetchStatistics();

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
                    updatePieChart(myTripsCount,110);

                    // Étape 2 : Calculer le montant total encaissé pour les trajets réalisés
                    tripSnapshots.getDocuments().forEach(tripDoc -> {
                        String tripId = tripDoc.getId();

                        firestore.collection("bookings")
                                .whereEqualTo("tripId", tripId)
                                .get()
                                .addOnSuccessListener(bookingSnapshots -> {
                                    double totalAmount = 0.0;

                                    for (var bookingDoc : bookingSnapshots) {
                                        Double tripAmount = bookingDoc.getDouble("contribution");
                                        if (tripAmount != null) {
                                            totalAmount += tripAmount;
                                        }
                                    }

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
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate();
    }
}
