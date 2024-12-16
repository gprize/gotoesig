package com.example.gotoesig.ui.searchtrips;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gotoesig.model.Trip;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;
import com.example.gotoesig.R;

public class SearchTripFragment extends Fragment {

    private EditText etStartPoint, etDate;
    private Button btnSearch;
    private RecyclerView rvTrips;
    private TripAdapter tripAdapter;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_trip, container, false);

        etStartPoint = view.findViewById(R.id.et_start_point);
        etDate = view.findViewById(R.id.et_date);
        btnSearch = view.findViewById(R.id.btn_search);
        rvTrips = view.findViewById(R.id.rv_trips);


        firestore = FirebaseFirestore.getInstance();
        tripAdapter = new TripAdapter(null);

        rvTrips.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTrips.setAdapter(tripAdapter);

        btnSearch.setOnClickListener(v -> searchTrips());

        setupDateTimePickers();

        return view;
    }

    private void setupDateTimePickers() {

        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year1, month1, dayOfMonth) -> etDate.setText(String.format("%04d-%02d-%02d", year1, month1 + 1, dayOfMonth)),
                    year, month, day);
            datePickerDialog.show();
        });
    }

    private void searchTrips() {
        String startPoint = etStartPoint.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        if (startPoint.isEmpty() || date.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Requête à Firestore pour trouver les trajets correspondant
        firestore.collection("trips")
                .whereEqualTo("startPoint", startPoint)
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Trip> trips = queryDocumentSnapshots.toObjects(Trip.class);
                    if (trips.isEmpty()) {
                        Toast.makeText(getContext(), "Aucun trajet trouvé", Toast.LENGTH_SHORT).show();
                    } else {
                        tripAdapter.updateTrips(trips);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur lors de la récupération des trajets", Toast.LENGTH_SHORT).show();
                });
    }

}
