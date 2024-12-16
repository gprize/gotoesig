package com.example.gotoesig.model;

import java.util.ArrayList;
import java.util.List;

public class Booking {
    private String tripId;
    private List<String> userIds;

    public Booking(String tripId) {
        this.tripId = tripId;
        this.userIds = new ArrayList<>();
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public void addUser(String userId) {
        if (!userIds.contains(userId)) {
            userIds.add(userId);
        }
    }
}
