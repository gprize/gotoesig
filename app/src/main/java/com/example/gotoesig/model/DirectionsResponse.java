package com.example.gotoesig.model;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import com.google.android.gms.maps.model.LatLng;

public class DirectionsResponse {

    public static List<List<LatLng>> parse(String jsonResponse) {
        List<List<LatLng>> routes = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray routesArray = jsonObject.getJSONArray("routes");

            for (int i = 0; i < routesArray.length(); i++) {
                JSONObject route = routesArray.getJSONObject(i);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String points = overviewPolyline.getString("points");
                routes.add(decodePolyline(points));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return routes;
    }

    private static List<LatLng> decodePolyline(String encoded) {
        List<LatLng> polyline = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dLat = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            lat += dLat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dLng = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            lng += dLng;

            LatLng position = new LatLng(lat / 1E5, lng / 1E5);
            polyline.add(position);
        }
        return polyline;
    }
}
