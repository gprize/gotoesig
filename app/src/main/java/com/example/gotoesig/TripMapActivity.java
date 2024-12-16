package com.example.gotoesig;

import android.net.Uri;
import android.os.Bundle;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.os.AsyncTask;

public class TripMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private String startPoint;
    private String endPoint;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_map);

        startPoint = getIntent().getStringExtra("startPoint");
        endPoint = getIntent().getStringExtra("endPoint");
        mode = getIntent().getStringExtra("mode");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Utiliser l'API Directions pour tracer l'itinéraire
        String url = getDirectionsUrl(startPoint, endPoint, mode);
        new DirectionsFetcher().execute(url);
    }

    // Fonction pour générer l'URL pour l'API Directions
    private String getDirectionsUrl(String origin, String destination, String mode) {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=" + Uri.encode(origin)
                + "&destination=" + Uri.encode(destination)
                + "&mode=" + mode.toLowerCase()
                + "&key=AIzaSyCH58MZE4ZXK2XKdXIn90wOq3aHERn0GOI";
    }

    // AsyncTask pour récupérer les directions et afficher la polyline sur la carte
    private class DirectionsFetcher extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String data = "";
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                // Créer l'URL et effectuer la connexion
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                // Lire la réponse
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                data = stringBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                // Parser de la réponse JSON
                JSONObject jsonObject = new JSONObject(result);
                JSONArray routes = jsonObject.getJSONArray("routes");

                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);

                    // Récupérer l'itinéraire
                    JSONArray steps = leg.getJSONArray("steps");
                    ArrayList<LatLng> points = new ArrayList<>();

                    for (int i = 0; i < steps.length(); i++) {
                        JSONObject step = steps.getJSONObject(i);
                        String polyline = step.getJSONObject("polyline").getString("points");
                        points.addAll(decodePoly(polyline));
                    }

                    // Ajouter la polyline sur la carte
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .addAll(points)
                            .color(Color.BLUE)
                            .width(10);
                    mMap.addPolyline(polylineOptions);

                    // Centrer la carte sur le point de départ
                    JSONObject startLocation = leg.getJSONObject("start_location");
                    LatLng startLatLng = new LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 12));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Méthode pour décoder la polyline en une liste de LatLng
    private ArrayList<LatLng> decodePoly(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            poly.add(new LatLng(lat / 1E5, lng / 1E5));
        }
        return poly;
    }
}
