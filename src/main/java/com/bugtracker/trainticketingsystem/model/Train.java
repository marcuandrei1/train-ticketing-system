package com.trainticket.model;

public class Train {
    private final String id;
    private String name;
    private int totalSeats;
    private TrainType type;

    public enum TrainType {
        EXPRESS, REGIONAL, INTERCITY, HIGH_SPEED
    }

    public Train(String id, String name, int totalSeats, TrainType type) {
        this.id = id;
        this.name = name;
        this.totalSeats = totalSeats;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public TrainType getType() {
        return type;
    }

    public void setType(TrainType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s, %d seats", name, id, type, totalSeats);
    }
}
