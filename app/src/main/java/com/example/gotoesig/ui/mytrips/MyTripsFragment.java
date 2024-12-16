package com.example.gotoesig.ui.mytrips;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gotoesig.R;
import com.example.gotoesig.model.Trip;
import com.example.gotoesig.ui.searchtrips.TripAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MyTripsFragment extends Fragment {

    private MyTripsViewModel mViewModel;
    private RecyclerView recyclerView;
    private TextView emptyMessageTextView;
    private TripAdapter tripAdapter;

    public static MyTripsFragment newInstance() {
        return new MyTripsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_trips, container, false);

        recyclerView = view.findViewById(R.id.recycler_my_trips);
        emptyMessageTextView = view.findViewById(R.id.tv_empty_message);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tripAdapter = new TripAdapter(null); // Pas de clic spécifique ici
        recyclerView.setAdapter(tripAdapter);

        fetchUserTrips(); // Récupération des données utilisateur

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MyTripsViewModel.class);
        // Vous pouvez utiliser le ViewModel ici si besoin
    }

    private void fetchUserTrips() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        String userId = auth.getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Vous devez être connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("bookings")
                .whereArrayContains("userIds", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        emptyMessageTextView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        List<String> tripIds = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            tripIds = queryDocumentSnapshots.getDocuments()
                                    .stream()
                                    .map(doc -> doc.getString("tripId"))
                                    .toList();
                        }else{
                            Toast.makeText(getContext(), "problème de sdk", Toast.LENGTH_SHORT).show();
                        }

                        fetchTripsDetails(tripIds);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur lors de la récupération des trajets", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchTripsDetails(List<String> tripIds) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("trips")
                .whereIn(FieldPath.documentId(), tripIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        emptyMessageTextView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        List<Trip> trips = queryDocumentSnapshots.toObjects(Trip.class);
                        tripAdapter.updateTrips(trips);

                        recyclerView.setVisibility(View.VISIBLE);
                        emptyMessageTextView.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur lors de la récupération des trajets détaillés", Toast.LENGTH_SHORT).show();
                });
    }
}
