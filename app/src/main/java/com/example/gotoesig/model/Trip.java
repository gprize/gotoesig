package com.example.gotoesig.model;

public class Trip {
    private String startPoint;
    private String endPoint;
    private String distance;
    private String duration;
    private String time;
    private String date;
    private String tolerance;
    private String seats; // Reste une String
    private String mode;
    private double contribution; // Double pour éviter les problèmes de désérialisation
    private String userId;

    public Trip() {}

    public Trip(String startPoint, String endPoint, String distance, String duration, String time, String date,
                String tolerance, String seats, String mode, double contribution, String userId) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.distance = distance;
        this.duration = duration;
        this.time = time;
        this.date = date;
        this.tolerance = tolerance;
        this.seats = seats;
        this.mode = mode;
        this.contribution = contribution;
        this.userId = userId;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTolerance() {
        return tolerance;
    }

    public void setTolerance(String tolerance) {
        this.tolerance = tolerance;
    }

    public String getSeats() {
        return seats;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public double getContribution() {
        return contribution;
    }

    public void setContribution(double contribution) {
        this.contribution = contribution;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
