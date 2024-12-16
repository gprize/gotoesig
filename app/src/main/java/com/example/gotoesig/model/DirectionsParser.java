package com.example.gotoesig.model;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.List;

public class DirectionsParser {

    public static HashMap<String, String> parseDurationAndDistance(String jsonResponse) {
        HashMap<String, String> result = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray routes = jsonObject.getJSONArray("routes");
            JSONObject route = routes.getJSONObject(0);

            JSONObject legs = route.getJSONArray("legs").getJSONObject(0);
            String distance = legs.getJSONObject("distance").getString("text");
            String duration = legs.getJSONObject("duration").getString("text");

            result.put("distance", distance);
            result.put("duration", duration);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
