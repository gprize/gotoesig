package com.example.gotoesig.ui.addtrip;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.gotoesig.R;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTripFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private Spinner transportSpinner;
    private EditText contributionEditText;
    private EditText timePickerEditText;
    private EditText datePickerEditText;
    private EditText delayToleranceEditText;
    private EditText availableSeatsEditText;

    private String selectedDate = "";
    private String selectedTime = "";
    private String startPoint = "";

    private final String apikey = "AIzaSyCH58MZE4ZXK2XKdXIn90wOq3aHERn0GOI";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publish_trip, container, false);

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), apikey);
        }

        setupAutocomplete(view);

        transportSpinner = view.findViewById(R.id.transportSpinner);
        contributionEditText = view.findViewById(R.id.contribution);
        timePickerEditText = view.findViewById(R.id.time_picker);
        datePickerEditText = view.findViewById(R.id.date_picker);
        delayToleranceEditText = view.findViewById(R.id.delayTolerance);
        availableSeatsEditText = view.findViewById(R.id.availableSeats);

        setupTransportSpinner();
        setupDateAndTimePickers(view);
        setupSaveButton(view);

        return view;
    }

    private void setupAutocomplete(View view) {
        // Ajout programmatique du fragment AutocompleteSupportFragment
        AutocompleteSupportFragment startPointFragment = new AutocompleteSupportFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.autocomplete_startPoint, startPointFragment)
                .commit();

        startPointFragment.setPlaceFields(List.of(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        startPointFragment.setHint("Point de départ");
        startPointFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                startPoint = place.getName();
                Log.d("AddTripFragment", "Point de départ sélectionné : " + place.getName());
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e("AddTripFragment", "Erreur avec le point de départ : " + status.getStatusMessage());
            }
        });
    }

    private void setupTransportSpinner() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("transportModes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> transportModes = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    transportModes.add(document.getString("mode"));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, transportModes);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                transportSpinner.setAdapter(adapter);
            } else {
                Log.w("Firestore", "Error getting transport modes.", task.getException());
            }
        });

        transportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTransport = parent.getItemAtPosition(position).toString();
                contributionEditText.setVisibility(
                        selectedTransport.equalsIgnoreCase("voiture") ? View.VISIBLE : View.GONE
                );
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                contributionEditText.setVisibility(View.GONE);
            }
        });
    }

    private void setupDateAndTimePickers(View view) {
        Button btnDatePicker = view.findViewById(R.id.btn_pickDate);
        btnDatePicker.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), this,
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        Button btnTimePicker = view.findViewById(R.id.btn_pickTime);
        btnTimePicker.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(requireContext(), this,
                    c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });
    }

    private void setupSaveButton(View view) {
        Button btnSaveTrip = view.findViewById(R.id.btn_saveTrip);
        btnSaveTrip.setOnClickListener(v -> saveTripToFirestore());
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        selectedDate = day + "/" + (month + 1) + "/" + year;
        datePickerEditText.setText(selectedDate);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        selectedTime = String.format("%02d:%02d", hourOfDay, minute);
        timePickerEditText.setText(selectedTime);
    }

    private void saveTripToFirestore() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Utilisateur non authentifié", Toast.LENGTH_SHORT).show();
            return;
        }

        String transportMode = transportSpinner.getSelectedItem().toString();
        String time = timePickerEditText.getText().toString();
        String date = datePickerEditText.getText().toString();
        String delayTolerance = delayToleranceEditText.getText().toString();
        String availableSeats = availableSeatsEditText.getText().toString();
        String contribution = contributionEditText.getVisibility() == View.VISIBLE
                ? contributionEditText.getText().toString()
                : "0";

        if (startPoint.isEmpty() || time.isEmpty() || date.isEmpty() || delayTolerance.isEmpty() || availableSeats.isEmpty()) {
            Toast.makeText(getContext(), "Tous les champs sont obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> trip = new HashMap<>();
        trip.put("userId", userId);
        trip.put("transportMode", transportMode);
        trip.put("startPoint", startPoint);
        trip.put("time", time);
        trip.put("date", date);
        trip.put("delayTolerance", Integer.parseInt(delayTolerance));
        trip.put("availableSeats", Integer.parseInt(availableSeats));
        trip.put("contribution", Float.parseFloat(contribution));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("trips").add(trip).addOnSuccessListener(documentReference -> {
            Toast.makeText(getContext(), "Trajet ajouté avec succès", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Erreur lors de l'ajout du trajet", Toast.LENGTH_SHORT).show();
        });
    }
}
