package com.example.model;

public class Vehicle {
    private String id;
    private String make;
    private String model;
    private double batteryCapacity;
    private int bestRange;
    private int worstRange;

    public Vehicle(String id, String make, String model, double batteryCapacity, int bestRange, int worstRange) {
        this.id = id;
        this.make = make;
        this.model = model;
        this.batteryCapacity = batteryCapacity;
        this.bestRange = bestRange;
        this.worstRange = worstRange;
    }

    // Getters
    public String getId() { return id; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public double getBatteryCapacity() { return batteryCapacity; }
    public int getBestRange() { return bestRange; }
    public int getWorstRange() { return worstRange; }
}
