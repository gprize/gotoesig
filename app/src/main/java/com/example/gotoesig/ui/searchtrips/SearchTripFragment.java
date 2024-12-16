package com.example.gotoesig.ui.searchtrips;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gotoesig.R;
import com.example.gotoesig.TripMapActivity;
import com.example.gotoesig.model.Trip;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.GeoApiContext;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class SearchTripFragment extends Fragment {

    private EditText etDate;
    private Button btnSearch;
    private RecyclerView rvTrips;
    private TripAdapter tripAdapter;
    private FirebaseFirestore firestore;
    private String startPoint;
    private double startLatitude;
    private double startLongitude;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_trip, container, false);

        etDate = view.findViewById(R.id.et_date);
        btnSearch = view.findViewById(R.id.btn_search);
        rvTrips = view.findViewById(R.id.rv_trips);


        firestore = FirebaseFirestore.getInstance();

        rvTrips.setLayoutManager(new LinearLayoutManager(getContext()));

        tripAdapter = new TripAdapter(trip -> {
            // Lorsque l'utilisateur clique sur un trajet
            Intent intent = new Intent(getContext(), TripMapActivity.class);
            intent.putExtra("startPoint", trip.getStartPoint());
            intent.putExtra("endPoint", trip.getEndPoint());
            intent.putExtra("mode", trip.getMode());
            startActivity(intent);
        });
        rvTrips.setAdapter(tripAdapter);

        btnSearch.setOnClickListener(v -> searchTrips());

        setupDateTimePickers();
        setupAutocomplete(view);

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

                            // Appeler la méthode pour obtenir l'adresse complète
                            getCompleteAddress(startLatitude, startLongitude);
                        }

                    }

                    @Override
                    public void onError(@NonNull Status status) {
                        Toast.makeText(getContext(), "Erreur: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getCompleteAddress(double latitude, double longitude) {
        try {
            GeoApiContext geoApiContext = new GeoApiContext.Builder()
                    .apiKey("AIzaSyCH58MZE4ZXK2XKdXIn90wOq3aHERn0GOI") // Utiliser votre clé API ici
                    .build();

            // Créez un objet LatLng avec les coordonnées
            com.google.maps.model.LatLng location = new com.google.maps.model.LatLng(latitude, longitude);

            // Utilisez l'API Geocoding pour obtenir l'adresse complète
            com.google.maps.model.GeocodingResult[] results =
                    com.google.maps.GeocodingApi.reverseGeocode(geoApiContext, location).await();

            // Vérifier si des résultats sont trouvés
            if (results != null && results.length > 0) {
                // Utiliser le premier résultat (ou parcourir le tableau si nécessaire)
                String fullAddress = results[0].formattedAddress;
                startPoint = fullAddress;  // Mise à jour du point de départ avec l'adresse complète
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Erreur lors de la récupération de l'adresse complète", Toast.LENGTH_SHORT).show();
        }
    }
}
