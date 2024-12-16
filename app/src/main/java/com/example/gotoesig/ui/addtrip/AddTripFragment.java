package com.example.gotoesig.ui.addtrip;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.gotoesig.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddTripFragment extends Fragment {

    private EditText timePickerEditText, datePickerEditText, delayToleranceEditText, availableSeatsEditText;
    private Spinner transportModeSpinner;
    private Button addTripButton;
    private String startPoint;
    private final String endPoint = "ESIGELEC, Rouen, France"; // Point d'arrivée fixe
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private EditText contributionEditText;
    private double startLatitude;
    private double startLongitude;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publish_trip, container, false);

        db = FirebaseFirestore.getInstance();
        transportModeSpinner = view.findViewById(R.id.transportModeSpinner);
        timePickerEditText = view.findViewById(R.id.timePickerEditText);
        datePickerEditText = view.findViewById(R.id.datePickerEditText);
        delayToleranceEditText = view.findViewById(R.id.delayToleranceEditText);
        availableSeatsEditText = view.findViewById(R.id.availableSeatsEditText);
        addTripButton = view.findViewById(R.id.addTripButton);
        progressBar = view.findViewById(R.id.progressBar);
        contributionEditText = view.findViewById(R.id.contribution);

        setupAutocomplete(view);
        setupDateTimePickers();
        setupTransportSpinner();

        addTripButton.setOnClickListener(v -> {
            if (isInputValid()) {
                callDistanceMatrixAPI(startLatitude, startLongitude, endPoint);
            }
        });

        return view;
    }

    private void setupAutocomplete(View view) {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        Places.initialize(getContext(), "AIzaSyCH58MZE4ZXK2XKdXIn90wOq3aHERn0GOI");
        AutocompleteSupportFragment autocompleteStart =
                (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.startPointAutocomplete);

        autocompleteStart.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
                .setOnPlaceSelectedListener(new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(@NonNull Place place) {
                        startPoint = place.getName();
                        LatLng startLatLng = place.getLatLng();  // récupère la latitude et la longitude
                        if (startLatLng != null) {
                            startLatitude = startLatLng.latitude;
                            startLongitude = startLatLng.longitude;
                            // Utiliser ces coordonnées pour appeler l'API Distance Matrix
                        }

                    }

                    @Override
                    public void onError(@NonNull Status status) {
                        Toast.makeText(getContext(), "Erreur: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupDateTimePickers() {
        timePickerEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    (view, hourOfDay, minuteOfHour) -> timePickerEditText.setText(String.format("%02d:%02d", hourOfDay, minuteOfHour)),
                    hour, minute, true);
            timePickerDialog.show();
        });

        datePickerEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year1, month1, dayOfMonth) -> datePickerEditText.setText(String.format("%04d-%02d-%02d", year1, month1 + 1, dayOfMonth)),
                    year, month, day);
            datePickerDialog.show();
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
                transportModeSpinner.setAdapter(adapter);
            } else {
                Log.w("Firestore", "Error getting transport modes.", task.getException());
            }
        });

        transportModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

    private boolean isInputValid() {
        if (startPoint == null || startPoint.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez renseigner un point de départ", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (timePickerEditText.getText().toString().isEmpty() || datePickerEditText.getText().toString().isEmpty() ||
                delayToleranceEditText.getText().toString().isEmpty() || availableSeatsEditText.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Tous les champs sont obligatoires", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void callDistanceMatrixAPI(double startLatitude, double startLongitude, String destination) {
        try {
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey("AIzaSyCH58MZE4ZXK2XKdXIn90wOq3aHERn0GOI")
                    .build();

            // Appeler l'API Distance Matrix
            DistanceMatrixApiRequest request = DistanceMatrixApi.newRequest(context);
            DistanceMatrix response = request.origins(new com.google.maps.model.LatLng(startLatitude, startLongitude))
                    .destinations(destination)
                    .await();

            // Vérifier que la réponse contient des lignes
            if (response.rows != null && response.rows.length > 0) {
                DistanceMatrixRow row = response.rows[0]; // Première ligne

                // Vérifier que la ligne contient des éléments
                if (row.elements != null && row.elements.length > 0) {
                    DistanceMatrixElement element = row.elements[0]; // Premier élément
                    showConfirmationDialog(element.distance.humanReadable, element.duration.humanReadable);
                } else {
                    Toast.makeText(getContext(), "Aucun élément trouvé dans la ligne.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Aucune ligne trouvée dans la réponse.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Erreur lors de l'appel à l'API Distance Matrix ", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private void saveTripToFirestore(String distance, String duration) {
        double contribution = contributionEditText.getVisibility() == View.VISIBLE
                ? Double.parseDouble(contributionEditText.getText().toString())
                : 0.0;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> trip = new HashMap<>();
        trip.put("startPoint", startPoint);
        trip.put("endPoint", endPoint);
        trip.put("distance", distance);
        trip.put("duration", duration);
        trip.put("time", timePickerEditText.getText().toString());
        trip.put("date", datePickerEditText.getText().toString());
        trip.put("tolerance", delayToleranceEditText.getText().toString());
        trip.put("seats", availableSeatsEditText.getText().toString());
        trip.put("mode", transportModeSpinner.getSelectedItem().toString());
        trip.put("contribution", contribution);
        trip.put("userId", userId);

        db.collection("trips").add(trip).addOnSuccessListener(docRef -> {
            Toast.makeText(getContext(), "Trajet ajouté avec succès", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Erreur lors de l'ajout du trajet", Toast.LENGTH_SHORT).show();
        });
    }

    private void showConfirmationDialog(String distance, String duration) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirmer le trajet");
        builder.setMessage("Distance : " + distance + "\nDurée : " + duration + "\nConfirmer l'ajout ?");

        builder.setPositiveButton("Confirmer", (dialog, which) -> {
            saveTripToFirestore(distance, duration); // Enregistrer le trajet dans la base
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> {
            dialog.dismiss(); // Fermer le dialogue
        });

        builder.show();
    }

}

